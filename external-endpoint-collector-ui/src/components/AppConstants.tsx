export const EXT_ENDPOINT_BACKEND_URL: string = 'http://extendpoint.local:6082/ext-endpoint-collector/endpoints'
export const ROOT_BREADCRUMB: string = 'Endpoints'
export const SAMPLE_ENDPOINT_DATA: {[id: string]: any} = {
    "application": "Endpoint collector 01",
    "taskName": "Collect data",
    "noAttemptTimes": 10000,
    "noParallelThread": 15,
    "extEndpoint": "<Targeting URL>",
    "extEndpointMethod": "POST",
    "extEndpointData": `{
        <POST DATA>
}`,
    "columnMetadata": `{
    \"columnId\": \"column1\", 
    \"columnMetadata\": [
        {\"fieldPath\": \"random\", \"mappingColumnName\":\"column1\"}, 
        {\"fieldPath\": \"$.data.user_id\", \"mappingColumnName\":\"column2\"}, 
        {\"fieldPath\": \"$.data.access_token\", \"mappingColumnName\":\"column3\"}, 
        {\"fieldPath\": \"$.data.email\", \"mappingColumnName\":\"column4\"},
        {\"fieldPath\": \"$.data.time_created\", \"mappingColumnName\":\"column5\"},
        {\"fieldPath\": \"$.data.time_expired\", \"mappingColumnName\":\"column6\"},
        {\"fieldPath\": \"$.data.name\", \"mappingColumnName\":\"column7\"},
        {\"fieldPath\": \"$.data.access_token\", \"decryptFunctionName\":\"decryptJWTBase64\", \"mappingColumnName\":\"column10\"}
    ]
}`,
    "generatorSaltLength": 10,
    "generatorSaltStartWith": "0392013890",
    "generatorStrategy": "SEQUENCE",
    "successCriteria": "user_id",
    "responseConsumerType": "DATABASE"
}


export interface ExtEndpointOverview {
    endpointId: number
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
    requestInfo: RequestInfoMeta
    columnMetadata: string
    dataGeneratorInfo: DataGeneratorInfoMeta
}