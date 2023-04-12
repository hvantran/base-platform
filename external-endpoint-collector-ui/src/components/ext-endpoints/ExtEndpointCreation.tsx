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
          propName: 'generatorSaltLength',
          propLabel: 'Gen data length',
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
          propLabel: 'Gen data start with',
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
          propLabel: 'Gen data strategy',
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
          propName: 'extEndpointData',
          propLabel: 'Data',
          propValue: '{}',
          propDefaultValue: '{}',
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
        let endpointMetadata: ExtEndpointMetadata = getActionFromStepper(currentStepMetadata);

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

  const getActionFromStepper = (currentStepMetadata: Array<StepMetadata>) => {
    const endpointMetadataStepIndex = 0;
    let endpointMetadataMetadata = currentStepMetadata.at(endpointMetadataStepIndex);
    if (!endpointMetadataMetadata) {
      throw new Error("Missing endpointMetadata definition");
    }
    const findStepPropertyByCondition = (stepMetadata: StepMetadata | undefined, filter: (property: PropertyMetadata) => boolean): PropertyMetadata | undefined => {
      return stepMetadata ? stepMetadata.properties.find(filter) : undefined;
    }
    const getAction = (): ExtEndpointMetadata => {
      let endpointMetadataDescription = findStepPropertyByCondition(endpointMetadataMetadata, property => property.propName === "endpointMetadataDescription");
      let endpointMetadataDefinition: ExtEndpointMetadata = {
      }
      return endpointMetadataDefinition;
    }

    return getAction();
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