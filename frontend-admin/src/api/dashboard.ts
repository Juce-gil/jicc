import request from '@/utils/request'

export interface ChartRecord {
  name: string
  value: number
}

export const queryDashboardCount = () => request.get<ChartRecord[]>('/dashboard/staticCount')
export const queryShelvesTrend = (day = 7) => request.get<ChartRecord[]>(`/dashboard/productShelvesInfo/${day}`)
