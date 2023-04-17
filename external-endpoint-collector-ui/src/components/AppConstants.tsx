export const EXT_ENDPOINT_BACKEND_URL: string = 'http://extendpoint.local:6082/ext-endpoint-collector/endpoints'
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
    input: InputMetadata
    filter: FilterMetadata
    output: OutputMetadata
}

export interface FilterMetadata {
    successCriteria: string
}

export interface OutputMetadata {
    responseConsumerType: string
}

export interface DataGeneratorInfoMeta {
    generatorSaltLength: number
    generatorSaltStartWith: string
    generatorStrategy: string
}

export interface RequestInfoMeta {
    extEndpoint: string
    method: string
    data?: string
}
export interface RequestInfoMeta {
    extEndpoint: string
    method: string
    data?: string
}
export interface InputMetadata {
    application: string
    taskName: string
    noAttemptTimes: number
    noParallelThread: number
    requestInfor: RequestInfoMeta
    columnMetadata: string
    dataGeneratorInfo: DataGeneratorInfoMeta
}