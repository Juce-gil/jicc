package cn.kmbeast.service.impl;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.mapper.AddressMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.AddressQueryDto;
import cn.kmbeast.pojo.entity.Address;
import cn.kmbeast.service.AddressService;
import cn.kmbeast.utils.PhoneNumberValidator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 地址业务逻辑接口实现类
 */
@Service
public class AddressServiceImpl implements AddressService {

    @Resource
    private AddressMapper addressMapper;

    @Override
    public Result<String> save(Address address) {
        if (address == null) {
            return ApiResult.error("请求参数不能为空");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        normalizeAddress(address);
        String validationMessage = validateAddress(address);
        if (validationMessage != null) {
            return ApiResult.error(validationMessage);
        }
        address.setUserId(currentUserId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(false);
        }
        dealDefaultAddress(address, currentUserId);
        addressMapper.save(address);
        return ApiResult.success("收件地址新增成功");
    }

    @Override
    public Result<String> update(Address address) {
        if (address == null) {
            return ApiResult.error("请求参数不能为空");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        if (address.getId() == null) {
            return ApiResult.error("地址ID不能为空");
        }
        Address savedAddress = findUserAddressById(address.getId(), currentUserId);
        if (savedAddress == null) {
            return ApiResult.error("收件地址不存在或无权限操作");
        }
        normalizeAddress(address);
        String validationMessage = validateAddress(address);
        if (validationMessage != null) {
            return ApiResult.error(validationMessage);
        }
        address.setUserId(currentUserId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(Boolean.TRUE.equals(savedAddress.getIsDefault()));
        }
        dealDefaultAddress(address, currentUserId);
        addressMapper.update(address);
        return ApiResult.success("收件地址修改成功");
    }

    private void dealDefaultAddress(Address address, Integer currentUserId) {
        if (!Boolean.TRUE.equals(address.getIsDefault())) {
            return;
        }
        List<Address> addressList = queryUserAddresses(currentUserId);
        for (Address savedAddress : addressList) {
            if (savedAddress == null || savedAddress.getId() == null) {
                continue;
            }
            if (address.getId() != null && address.getId().equals(savedAddress.getId())) {
                continue;
            }
            if (!Boolean.TRUE.equals(savedAddress.getIsDefault())) {
                continue;
            }
            savedAddress.setIsDefault(false);
            addressMapper.update(savedAddress);
        }
    }

    @Override
    public Result<String> batchDelete(List<Integer> ids) {
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        List<Integer> deleteIds = sanitizeIds(ids);
        if (deleteIds.isEmpty()) {
            return ApiResult.success("没有需要删除的收件地址");
        }
        List<Integer> ownedIds = filterOwnedAddressIds(deleteIds, currentUserId);
        if (ownedIds.isEmpty()) {
            return ApiResult.error("收件地址不存在或无权限删除");
        }
        addressMapper.batchDelete(ownedIds);
        return ApiResult.success("收件地址删除成功");
    }

    @Override
    public Result<List<Address>> query(AddressQueryDto addressQueryDto) {
        AddressQueryDto safeQueryDto = addressQueryDto == null ? new AddressQueryDto() : addressQueryDto;
        int totalCount = addressMapper.queryCount(safeQueryDto);
        List<Address> addressList = addressMapper.query(safeQueryDto);
        return ApiResult.success(addressList == null ? new ArrayList<>() : addressList, totalCount);
    }

    @Override
    public Result<String> isDefault(Address address) {
        if (address == null || address.getId() == null) {
            return ApiResult.error("地址ID不能为空");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        Address savedAddress = findUserAddressById(address.getId(), currentUserId);
        if (savedAddress == null) {
            return ApiResult.error("收件地址不存在或无权限操作");
        }
        Address updateAddress = new Address();
        updateAddress.setId(savedAddress.getId());
        updateAddress.setUserId(currentUserId);
        updateAddress.setIsDefault(true);
        dealDefaultAddress(updateAddress, currentUserId);
        addressMapper.update(updateAddress);
        return ApiResult.success("设置默认地址成功");
    }

    private String validateAddress(Address address) {
        if (!StringUtils.hasText(address.getConcatPerson())) {
            return "收件人不能为空";
        }
        if (!StringUtils.hasText(address.getConcatPhone())) {
            return "收件电话不能为空";
        }
        if (!StringUtils.hasText(address.getGetAdr())) {
            return "收件地址不能为空";
        }
        if (!PhoneNumberValidator.isValidChinesePhoneNumber(address.getConcatPhone())) {
            return "收件电话不符合要求";
        }
        return null;
    }

    private void normalizeAddress(Address address) {
        if (address == null) {
            return;
        }
        if (address.getConcatPerson() != null) {
            address.setConcatPerson(address.getConcatPerson().trim());
        }
        if (address.getConcatPhone() != null) {
            address.setConcatPhone(address.getConcatPhone().trim());
        }
        if (address.getGetAdr() != null) {
            address.setGetAdr(address.getGetAdr().trim());
        }
    }

    private Address findUserAddressById(Integer addressId, Integer currentUserId) {
        if (addressId == null || currentUserId == null) {
            return null;
        }
        for (Address address : queryUserAddresses(currentUserId)) {
            if (address != null && addressId.equals(address.getId())) {
                return address;
            }
        }
        return null;
    }

    private List<Address> queryUserAddresses(Integer currentUserId) {
        AddressQueryDto queryDto = new AddressQueryDto();
        queryDto.setUserId(currentUserId);
        List<Address> addressList = addressMapper.query(queryDto);
        return addressList == null ? new ArrayList<>() : addressList;
    }

    private List<Integer> filterOwnedAddressIds(List<Integer> ids, Integer currentUserId) {
        Set<Integer> ownedIdSet = new LinkedHashSet<>();
        for (Address address : queryUserAddresses(currentUserId)) {
            if (address != null && address.getId() != null) {
                ownedIdSet.add(address.getId());
            }
        }
        List<Integer> result = new ArrayList<>();
        for (Integer id : ids) {
            if (ownedIdSet.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    private List<Integer> sanitizeIds(List<Integer> ids) {
        Set<Integer> idSet = new LinkedHashSet<>();
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        for (Integer id : ids) {
            if (id != null) {
                idSet.add(id);
            }
        }
        return new ArrayList<>(idSet);
    }
}
