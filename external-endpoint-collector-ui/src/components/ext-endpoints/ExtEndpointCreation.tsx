import { json } from '@codemirror/lang-json';
import { Stack } from '@mui/material';

import LinkBreadcrumd from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import React from 'react';
import { EXT_ENDPOINT_BACKEND_URL, ExtEndpointMetadata, ROOT_BREADCRUMB } from '../AppConstants';
import {
  PageEntityMetadata,
  PropType,
  PropertyMetadata,
  RestClient,
  SnackbarAlertMetadata,
  SnackbarMessage,
  StepMetadata
} from '../GenericConstants';
import ProcessTracking from '../common/ProcessTracking';
import SnackbarAlert from '../common/SnackbarAlert';
import PageEntityRender from '../renders/PageEntityRender';


export default function ActionCreation() {

  let initialStepsV3: Array<StepMetadata> = []
  const [openError, setOpenError] = React.useState(false);
  const [openSuccess, setOpenSuccess] = React.useState(false);
  const [messageInfo, setMessageInfo] = React.useState<SnackbarMessage | undefined>(undefined);
  const [processTracking, setCircleProcessOpen] = React.useState(false);
  const [stepMetadatas, setStepMetadatas] = React.useState(initialStepsV3);
  const restClient = new RestClient(setCircleProcessOpen, setMessageInfo, setOpenError, setOpenSuccess);

  let initialStepMetadatas: Array<StepMetadata> = [
    {
      name: "extEndpointCreation",
      label: 'Endpoint metadata',
      description: 'This step is used to define an external endpoint information',
      properties: [
        {
          propName: 'application',
          propLabel: 'Application',
          propValue: '',
          isRequired: true,
          layoutProperties: { xs: 12, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 2 },
          valueElementProperties: { xs: 10 },
          propDescription: 'The application name',
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;

              setStepMetadatas(onchangeStepDefault(propName, propValue, (stepMetadata) => {
                if (stepMetadata.name === 'extEndpointCreation') {
                  stepMetadata.label = propValue;
                }
              }));
            }
          }
        },
        {
          propName: 'taskName',
          propLabel: 'Task name',
          propValue: '',
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4 },
          valueElementProperties: { xs: 8 },
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'noAttemptTimes',
          propLabel: 'Number of attempts',
          propValue: '',
          propExtraProperties: {type: "number"},
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4, sx: { pl: 5 } },
          valueElementProperties: { xs: 8 },
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'noParallelThread',
          propLabel: 'Number of threads',
          propValue: '',
          propExtraProperties: {type: "number"},
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4 },
          valueElementProperties: { xs: 8 },
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'extEndpoint',
          propLabel: 'Targeting endpoint',
          propValue: '',
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4, sx: { pl: 5 } },
          valueElementProperties: { xs: 8 },
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'extEndpointMethod',
          propLabel: 'Http method',
          propValue: 'GET',
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4 },
          valueElementProperties: { xs: 8 },
          propType: PropType.Selection,
          selectionMeta: {
            selections: ["GET", "POST", "PUT", "DELETE"],
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue, undefined, (propertyMetadata) => {
                if (propertyMetadata.propName === 'extEndpointData') {
                  propertyMetadata.disabled = ["GET", "DELETE"].includes(propValue);
                }
              }));
            }
          }
        },
        {
          propName: 'generatorSaltLength',
          propLabel: 'Generator data length',
          propValue: '',
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4, sx: { pl: 5 } },
          valueElementProperties: { xs: 8 },
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'generatorSaltStartWith',
          propLabel: 'Generator start with',
          propValue: '',
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4 },
          valueElementProperties: { xs: 8 },
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'generatorStrategy',
          propLabel: 'Generator data strategy',
          propValue: 'NONE',
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4, sx: { pl: 5 } },
          valueElementProperties: { xs: 8 },
          propType: PropType.Selection,
          selectionMeta: {
            selections: ["RANDOM", "SEQUENCE", "RANDOM_WITH_CONDITION", "NONE"],
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'successCriteria',
          propLabel: 'Success contains text',
          propValue: '',
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4 },
          valueElementProperties: { xs: 8 },
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'responseConsumerType',
          propLabel: 'Response Type',
          propValue: 'CONSOLE',
          layoutProperties: { xs: 6, alignItems: "center", justifyContent: "center" },
          labelElementProperties: { xs: 4, sx: { pl: 5 } },
          valueElementProperties: { xs: 8 },
          propType: PropType.Selection,
          selectionMeta: {
            selections: ["CONSOLE", "DATABASE"],
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue, undefined, (propertyMetadata) => {
                if (propertyMetadata.propName === 'columnMetadata') {
                  propertyMetadata.disabled = !["DATABASE"].includes(propValue);
                }
              }));
            }
          }
        },
        {
          propName: 'extEndpointData',
          propLabel: 'Data',
          propValue: '{}',
          propDefaultValue: '{}',
          disabled: true,
          layoutProperties: { xs: 12 },
          labelElementProperties: { xs: 2 },
          valueElementProperties: { xs: 10 },
          isRequired: true,
          propType: PropType.CodeEditor,
          codeEditorMeta:
          {
            codeLanguges: [json()],
            onChangeEvent: function (propName) {
              return (value, _) => {
                let propValue = value;
                setStepMetadatas(onchangeStepDefault(propName, propValue))
              }
            }
          }
        },
        {
          propName: 'columnMetadata',
          propLabel: 'Output column metadata',
          propValue: '{}',
          propDefaultValue: '{}',
          disabled: true,
          layoutProperties: { xs: 12 },
          labelElementProperties: { xs: 2 },
          valueElementProperties: { xs: 10 },
          isRequired: true,
          propType: PropType.CodeEditor,
          codeEditorMeta:
          {
            codeLanguges: [json()],
            onChangeEvent: function (propName) {
              return (value, _) => {
                let propValue = value;
                setStepMetadatas(onchangeStepDefault(propName, propValue))
              }
            }
          }
        }
      ]
    },
    {
      name: "review",
      label: 'Review',
      description: 'This step is used to review all steps',
      properties: [],
      onFinishStepClick: async (currentStepMetadata: Array<StepMetadata>) => {
        let endpointMetadata: ExtEndpointMetadata = getExtEndpointMetadataFromStepper(currentStepMetadata);

        const requestOptions = {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(endpointMetadata)
        };

        const targetURL = `${EXT_ENDPOINT_BACKEND_URL}`;
        await restClient.sendRequest(requestOptions, targetURL, async (response) => {
          let responseJSON = await response.json();
          let message = `Action ${responseJSON['endpointMetadataId']} is created`;
          return { 'message': message, key: new Date().getTime() } as SnackbarMessage;
        }, async (response: Response) => {
          return { 'message': "An interal error occurred during your request!", key: new Date().getTime() } as SnackbarMessage;
        });
      }
    }
  ]

  React.useEffect(() => {
    setStepMetadatas(initialStepMetadatas);
  }, [])

  const getExtEndpointMetadataFromStepper = (currentStepMetadata: Array<StepMetadata>) => {
    const endpointMetadataStepIndex = 0;
    let endpointMetadataMetadata = currentStepMetadata.at(endpointMetadataStepIndex);
    if (!endpointMetadataMetadata) {
      throw new Error("Missing endpointMetadata definition");
    }
    const findStepPropertyByCondition = (stepMetadata: StepMetadata | undefined, filter: (property: PropertyMetadata) => boolean): PropertyMetadata | undefined => {
      return stepMetadata ? stepMetadata.properties.find(filter) : undefined;
    }
    const getExtEndpointMetadata = (): ExtEndpointMetadata => {
      let application = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "aplication")?.propValue;
      let taskName = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "taskName")?.propValue;
      let noAttemptTimes = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "noAttemptTimes")?.propValue;
      let noParallelThread = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "noParallelThread")?.propValue;
      let extEndpoint = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "extEndpoint")?.propValue;
      let method = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "extEndpointMethod")?.propValue;
      let data = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "extEndpointData")?.propValue;
      let columnMetadata = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "columnMetadata")?.propValue;
      let generatorSaltLength = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "generatorSaltLength")?.propValue;
      let generatorSaltStartWith = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "generatorSaltStartWith")?.propValue;
      let generatorStrategy = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "generatorStrategy")?.propValue;
      let successCriteria = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "successCriteria")?.propValue;
      let responseConsumerType = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "responseConsumerType")?.propValue;
      let endpointMetadataDefinition: ExtEndpointMetadata = {
        input: {
          application,
          taskName,
          noAttemptTimes,
          noParallelThread,
          requestInfor: {
            extEndpoint,
            method,
            data
          },
          columnMetadata,
          dataGeneratorInfo: {
            generatorSaltLength,
            generatorSaltStartWith,
            generatorStrategy
          }
        },
        filter: {
          successCriteria
        },
        output: {
          responseConsumerType
        }
      }
      return endpointMetadataDefinition;
    }

    return getExtEndpointMetadata();
  }

  let initialPageEntityMetdata: PageEntityMetadata = {
    pageName: 'endpointMetadata-creation',
    breadcumbsMeta: [
      <LinkBreadcrumd underline="hover" key="1" color="inherit" href="/endpoints">
        {ROOT_BREADCRUMB}
      </LinkBreadcrumd>,
      <Typography key="3" color="text.primary">new</Typography>
    ],
    stepMetadatas: stepMetadatas
  }


  let snackbarAlertMetadata: SnackbarAlertMetadata = {
    openError,
    openSuccess,
    setOpenError,
    setOpenSuccess,
    messageInfo
  }

  return (
    <Stack spacing={4}>
      <PageEntityRender {...initialPageEntityMetdata} />
      <ProcessTracking isLoading={processTracking}></ProcessTracking>
      <SnackbarAlert {...snackbarAlertMetadata}></SnackbarAlert>
    </Stack>
  );

  function onchangeStepDefault(propName: string, propValue: any, stepMetadataCallback?: (stepMetadata: StepMetadata) => void,
    propertyCallback?: (property: PropertyMetadata) => void): React.SetStateAction<StepMetadata[]> {
    return previous => {
      return [...previous].map((stepMetadata) => {
        let properties = stepMetadata.properties.map(prop => {
          if (prop.propName === propName) {
            prop.propValue = propValue;
          }
          if (propertyCallback) {
            propertyCallback(prop);
          }
          return prop;
        });

        stepMetadata.properties = properties;
        if (stepMetadataCallback) {
          stepMetadataCallback(stepMetadata);
        }
        return stepMetadata;
      });
    };
  }
}