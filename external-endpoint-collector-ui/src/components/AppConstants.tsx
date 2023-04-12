export const EXT_ENDPOINT_BACKEND_URL: string = 'http://localhost:8083/ext-endpoint-collector/rest-data/endpoints'
export const ROOT_BREADCRUMB: string = 'Endpoints'

export interface ExtEndpointOverview {
    application: string
    taskName: string
    noAttemptTimes: number
    noParallelThread: number
    extEndpoint: string
    extEndpointMethod: string
    extEndpointData: string
    successCriteria: string
}

export interface ExtEndpointResponseOverview {
    id: number
    column1: string
    column2: string
    column3: string
    column4: string
    column5: string
    column6: string
    column7: string
    column8: string
    column9: string
    column10: string
}

export interface ExtEndpointMetadata {
}