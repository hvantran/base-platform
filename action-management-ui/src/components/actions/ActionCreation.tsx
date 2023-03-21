import { javascript } from '@codemirror/lang-javascript';
import { json } from '@codemirror/lang-json';
import AddTaskTwoToneIcon from '@mui/icons-material/AddTaskTwoTone';
import ArrowBackTwoToneIcon from '@mui/icons-material/ArrowBackTwoTone';
import { Alert, Snackbar, Stack } from '@mui/material';
import { blue, green } from '@mui/material/colors';

import LinkBreadcrumd from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import * as React from 'react';
import {
  PageEntityMetadata,
  PropertyMetadata,
  PropType,
  SnackbarAlertMetadata,
  SnackbarMessage,
  SpeedDialActionMetadata,
  StepMetadata,
  WithLink
} from '../GenericConstants';
import PageEntityRender from '../renders/PageEntityRender';
import Breadcrumbs from '../common/Breadcrumbs';
import ProcessTracking from '../common/ProcessTracking';
import { ActionDefinition, ACTION_MANAGER_API_URL, JobDefinition } from '../AppConstants';
import SnackbarAlert from '../common/SnackbarAlert';


export default function ActionCreation() {

  let initialStepsV3: Array<StepMetadata> = []
  const [stepMetadatas, setStepMetadatas] = React.useState(initialStepsV3);

  let initialTemplateStep: StepMetadata = {
    name: "job",
    label: 'Job 1',
    description: 'This step is used to define job information',
    properties: [
      {
        propName: 'jobName',
        propLabel: 'Name',
        isRequired: true,
        propValue: '',
        propDescription: 'This is name of job',
        propType: PropType.InputText,
        textFieldMeta: {
          onChangeEvent: function (event) {
            let propValue = event.target.value;
            let propName = event.target.name;
            let jobIndex = propName.replace('jobName', '');

            setStepMetadatas(onchangeStepDefault(propName, propValue, (stepMetadata) => {
              if (stepMetadata.name === `job${jobIndex}`) {
                stepMetadata.label = propValue;
              }
            }))
          }
        }
      },
      {
        propName: 'isAsync',
        propLabel: 'Asynchronous',
        propValue: true,
        propType: PropType.Switcher,
        switcherFieldMeta: {
          onChangeEvent: function (event, propValue) {
            let propName = event.target.name;
            setStepMetadatas(onchangeStepDefault(propName, propValue))
          }
        }
      },
      {
        propName: 'jobCategory',
        propLabel: 'Category',
        propValue: 'NORMAL',
        propType: PropType.Selection,
        selectionMeta: {
          selections: ["SYSTEM", "NORMAL"],
          onChangeEvent: function (event) {
            let propValue = event.target.value;
            let propName = event.target.name;
            setStepMetadatas(onchangeStepDefault(propName, propValue))
          }
        }
      },
      {
        propName: 'jobDescription',
        propLabel: 'Description',
        propValue: true,
        propType: PropType.Textarea,
        textareaFieldMeta: {
          onChangeEvent: function (event) {
            let propValue = event.target.value;
            let propName = event.target.name;
            setStepMetadatas(onchangeStepDefault(propName, propValue))
          }
        }
      },
      {
        propName: 'jobConfigurations',
        propLabel: 'Configurations',
        isRequired: true,
        propValue: '',
        propType: PropType.CodeEditor,
        codeEditorMeta:
        {
          height: '100px',
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
        propName: 'jobContent',
        propLabel: 'Job Content',
        isRequired: true,
        propValue: '',
        propType: PropType.CodeEditor,
        codeEditorMeta:
        {
          codeLanguges: [javascript({ jsx: true })],
          onChangeEvent: function (propName) {
            return (value, _) => {
              let propValue = value;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        }
      }
    ]
  }
  let initialStepsV2: Array<StepMetadata> = [
    {
      name: "action",
      label: 'Action',
      description: 'This step is used to define action information',
      properties: [
        {
          propName: 'actionName',
          propLabel: 'Name',
          propValue: '',
          isRequired: true,
          propDescription: 'This is name of action',
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;

              setStepMetadatas(onchangeStepDefault(propName, propValue, (stepMetadata) => {
                if (stepMetadata.name === 'action') {
                  stepMetadata.label = propValue;
                }
              }));
            }
          }
        },
        {
          propName: 'actionDescription',
          propLabel: 'Description',
          propValue: '',
          propType: PropType.Textarea,
          textareaFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'actionConfigurations',
          propLabel: 'Configurations',
          propValue: '',
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
      name: "job",
      label: 'Job 1',
      description: 'This step is used to define job information',
      properties: [
        {
          propName: 'jobName',
          propLabel: 'Name',
          propValue: '',
          isRequired: true,
          propDescription: 'This is name of job',
          propType: PropType.InputText,
          textFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue, (stepMetadata) => {
                if (stepMetadata.name === 'job') {
                  stepMetadata.label = propValue;
                }
              }))
            }
          }
        },
        {
          propName: 'isAsync',
          propLabel: 'Asynchronous',
          propValue: true,
          propType: PropType.Switcher,
          switcherFieldMeta: {
            onChangeEvent: function (event, propValue) {
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'jobCategory',
          propLabel: 'Category',
          propValue: 'NORMAL',
          propType: PropType.Selection,
          selectionMeta: {
            selections: ["SYSTEM", "NORMAL"],
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'jobDescription',
          propLabel: 'Description',
          propValue: '',
          propType: PropType.Textarea,
          textareaFieldMeta: {
            onChangeEvent: function (event) {
              let propValue = event.target.value;
              let propName = event.target.name;
              setStepMetadatas(onchangeStepDefault(propName, propValue))
            }
          }
        },
        {
          propName: 'jobConfigurations',
          propLabel: 'Configurations',
          isRequired: true,
          propValue: '',
          propType: PropType.CodeEditor,
          codeEditorMeta:
          {
            height: '100px',
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
          propName: 'jobContent',
          propLabel: 'Job Content',
          isRequired: true,
          propValue: '',
          propType: PropType.CodeEditor,
          codeEditorMeta:
          {
            codeLanguges: [javascript({ jsx: true })],
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
      onFinishStepClick: (currentStepMetadata: Array<StepMetadata>) => {

        let action: ActionDefinition = getActionFromStepper(currentStepMetadata);
        const requestOptions = {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(action)
        };

        setCircleProcessOpen(true);
        fetch(ACTION_MANAGER_API_URL, requestOptions)
          .then(response => {
            if (response.ok) {
              return response.json().then(text => {
                let message = `Action ${text['actionId']} is created`;
                return { 'message': message, key: new Date().getTime() } as SnackbarMessage;
              })
            }
            return response.text().then(text => {
              let messageInfo = { 'message': "An interal error occurred during your request!", key: new Date().getTime() } as SnackbarMessage;
              throw new Error(JSON.stringify(messageInfo));
            });
          })
          .then(messageInfo => {
            setMessageInfo((prev) => messageInfo);
            setOpenSuccess(true);
          })
          .catch((error: Error) => {
            let messageInfo = JSON.parse(error.message) as SnackbarMessage;
            setMessageInfo((prev) => messageInfo);
            setOpenError(true);
          })
          .finally(() => {
            setCircleProcessOpen(false);
          });
      }
    }
  ]

  React.useEffect(() => {
    setStepMetadatas(initialStepsV2);
  }, [])

  const getActionFromStepper = (currentStepMetadata: Array<StepMetadata>) => {
    const actionStepIndex = 0;
    let actionMetadata = currentStepMetadata.at(actionStepIndex);
    if (!actionMetadata) {
      throw new Error("Missing action definition");
    }
    const findStepPropertyByCondition = (stepMetadata: StepMetadata | undefined, filter: (property: PropertyMetadata) => boolean): PropertyMetadata | undefined => {
      return stepMetadata ? stepMetadata.properties.find(filter) : undefined;
    }
    const getAction = (): ActionDefinition => {
      let actionDescription = findStepPropertyByCondition(actionMetadata, property => property.propName === "actionDescription");
      let actionConfigurations = findStepPropertyByCondition(actionMetadata, property => property.propName === "actionConfigurations");
      let relatedJobs = findRelatedJobs(currentStepMetadata);
      let actionDefinition: ActionDefinition = {
        name: actionMetadata?.label,
        description: actionDescription?.propValue,
        configurations: actionConfigurations?.propValue,
        relatedJobs: relatedJobs
      }
      return actionDefinition;
    }

    const findRelatedJobs = (currentStepMetadata: Array<StepMetadata>): Array<JobDefinition> => {
      const reviewStepIndex = currentStepMetadata.length - 1;
      return currentStepMetadata.filter((_, index) => index !== 0 && index !== reviewStepIndex)
        .map(stepMetadata => {

          let name = findStepPropertyByCondition(stepMetadata, property => property.propName.startsWith("jobName"))?.propValue;
          let description = findStepPropertyByCondition(stepMetadata, property => property.propName.startsWith("jobDescription"))?.propValue;
          let configurations = findStepPropertyByCondition(stepMetadata, property => property.propName.startsWith("jobConfigurations"))?.propValue;
          let content = findStepPropertyByCondition(stepMetadata, property => property.propName.startsWith("jobContent"))?.propValue;
          let isAsync = findStepPropertyByCondition(stepMetadata, property => property.propName.startsWith("isAsync"))?.propValue;
          let category = findStepPropertyByCondition(stepMetadata, property => property.propName.startsWith("jobCategory"))?.propValue;

          return {
            name,
            category,
            description,
            configurations,
            content,
            isAsync
          } as JobDefinition
        })
    }

    return getAction();
  }


  let initialFloatingActions: Array<SpeedDialActionMetadata> = [
    {
      actionIcon: <AddTaskTwoToneIcon />, actionName: 'newJob', actionLabel: 'New Job',
      onClick: () => {
        setStepMetadatas(previous => {
          let nextStepMetadata: Array<StepMetadata> = [...previous];
          let addtionalStep = { ...initialTemplateStep }
          let newStepName = `${initialTemplateStep.name}${nextStepMetadata.length}`
          addtionalStep.name = newStepName
          addtionalStep.properties.forEach(property => property.propName = `${property.propName}${nextStepMetadata.length}`)
          nextStepMetadata.splice(previous.length - 1, 0, addtionalStep);
          return nextStepMetadata;
        })
      },
      properties: {
        sx: {
          bgcolor: green[500],
          '&:hover': {
            bgcolor: green[800],
          }
        }
      },
    },
    {
      actionIcon: WithLink('/actions', <ArrowBackTwoToneIcon />), actionName: 'navigateBackToActions', actionLabel: 'Navigate to actions', properties: {
        sx: {
          bgcolor: blue[500],
          '&:hover': {
            bgcolor: blue[800],
          }
        }
      }
    }
  ];

  let initialPageEntityMetdata: PageEntityMetadata = {
    pageName: 'action-creation',
    floatingActions: initialFloatingActions,
    stepMetadatas: stepMetadatas
  }


  const breadcrumbs = [
    <LinkBreadcrumd underline="hover" key="1" color="inherit" href="/actions">Actions</LinkBreadcrumd>,
    <Typography key="3" color="text.primary">new</Typography>
  ]

  const [openError, setOpenError] = React.useState(false);
  const [openSuccess, setOpenSuccess] = React.useState(false);
  const [messageInfo, setMessageInfo] = React.useState<SnackbarMessage | undefined>(undefined);
  const [processTracking, setCircleProcessOpen] = React.useState(false);

  let snackbarAlertMetadata: SnackbarAlertMetadata = {
    openError,
    openSuccess,
    setOpenError,
    setOpenSuccess,
    messageInfo
  }

  return (
    <Stack spacing={4}>
      <Breadcrumbs breadcrumbs={breadcrumbs} />
      <PageEntityRender {...initialPageEntityMetdata} />
      <ProcessTracking isLoading={processTracking}></ProcessTracking>
      <SnackbarAlert {...snackbarAlertMetadata}></SnackbarAlert>
    </Stack>
  );

  function onchangeStepDefault(propName: string, propValue: any, mapFunction?: (stepMetadata: StepMetadata) => void): React.SetStateAction<StepMetadata[]> {
    return previous => {
      return [...previous].map((stepMetadata) => {
        let properties = stepMetadata.properties.map(prop => {
          if (prop.propName === propName) {
            prop.propValue = propValue;
          }
          return prop;
        });

        stepMetadata.properties = properties;
        if (mapFunction) {
          mapFunction(stepMetadata);
        }
        return stepMetadata;
      });
    };
  }
}