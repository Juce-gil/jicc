import request from '@/utils/request'

export const queryDashboardCount = () => request.get('/dashboard/staticCount')
export const queryShelvesTrend = (day = 7) => request.get(`/dashboard/productShelvesInfo/${day}`)
