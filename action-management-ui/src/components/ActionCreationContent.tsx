import { javascript } from '@codemirror/lang-javascript';
import { json } from '@codemirror/lang-json';
import { LanguageSupport } from '@codemirror/language';
import { ViewUpdate } from "@codemirror/view";
import AddTaskTwoToneIcon from '@mui/icons-material/AddTaskTwoTone';
import ArrowBackTwoToneIcon from '@mui/icons-material/ArrowBackTwoTone';
import { Grid, Stack, TextField } from '@mui/material';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import { blue, green } from '@mui/material/colors';
import LinkBreadcrumd from '@mui/material/Link';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Stepper from '@mui/material/Stepper';
import Typography from '@mui/material/Typography';
import * as React from 'react';
import withLink, { PageEntityMetadata, PropType, SpeedDialActionMetadata, StepMetadata } from './AppConstants';
import { PageEntityToReactNodes } from './AppStepperRender';
import Breadcrumbs from './Breadcrumbs';
import CodeEditor from './CodeEditor';
import FloatingSpeedDialButtons from './FloatingActions';



export interface StepDefinition {
  name: string;
  label: string;
  value: string;
  description?: string;
  requiredCodeBlock?: boolean;
  codeLanguges?: Array<LanguageSupport>;
}


let initialSteps: Array<StepDefinition> = [
  { name: "action", label: 'Action', value: "", requiredCodeBlock: true, codeLanguges: [json()] },
  { name: "job", label: 'Job 1', value: "", requiredCodeBlock: true, codeLanguges: [javascript({ jsx: true })] },
  { name: "review", label: 'Review', value: "" }
];

let initialStepsV2: Array<StepMetadata> = [
  {
    name: "action",
    label: 'Action',
    description: 'This step is used to define action information',
    properties: [
      {
        propName: 'actionName', 
        propLabel: 'Name', 
        propDescription: 'This is name of action',
        propType: PropType.InputText
      },
      {
        propName: 'actionDescription', 
        propLabel: 'Description', 
        propType: PropType.Textarea
      },
      {
        propName: 'actionConfigurations', 
        propLabel: 'Configurations', 
        propType: PropType.CodeEditor,
        codeEditorMeta: 
        {
          codeLanguges: [json()],
          onChangeEvent: () => {}
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
        propName: 'actionName', 
        propLabel: 'Name', 
        propDescription: 'This is name of action',
        propType: PropType.InputText
      },
      {
        propName: 'actionDescription', 
        propLabel: 'Description', 
        propType: PropType.Textarea
      },
      {
        propName: 'actionConfigurations', 
        propLabel: 'Configurations', 
        propType: PropType.CodeEditor,
        codeEditorMeta: 
        {
          codeLanguges: [json()],
          onChangeEvent: () => {}
        }
      }
    ]
  },
  {
    name: "review",
    label: 'Review',
    description: 'This step is used to review all steps',
    properties: [
    ]
  }
]


let actions: Array<SpeedDialActionMetadata> = [
  {
    icon: <AddTaskTwoToneIcon />, name: 'New Job', properties: {
      sx: {
        bgcolor: green[500],
        '&:hover': {
          bgcolor: green[800],
        }
      }
    },
    onClick: onNewJobActionClick
  },
  {
    icon: withLink('/actions', <ArrowBackTwoToneIcon />), name: 'Navigate to actions', properties: {
      sx: {
        bgcolor: blue[500],
        '&:hover': {
          bgcolor: blue[800],
        }
      }
    }
  }
];

let pageEntityMetdata: PageEntityMetadata = {
  floatingActions
}

export default function ActionCreationContent() {
  // let [steps, setSteps] = React.useState(initialSteps);
  // const [activeStep, setActiveStep] = React.useState(0);
  // const [skipped, setSkipped] = React.useState(new Set<number>());

  // const isStepOptional = (step: number) => {
  //   return step === 1;
  // };

  // const isStepSkipped = (step: number) => {
  //   return skipped.has(step);
  // };

  // const handleNext = () => {
  //   let newSkipped = skipped;
  //   if (isStepSkipped(activeStep)) {
  //     newSkipped = new Set(newSkipped.values());
  //     newSkipped.delete(activeStep);
  //   }

  //   setActiveStep((prevActiveStep) => prevActiveStep + 1);
  //   setSkipped(newSkipped);
  // };

  // const handleBack = () => {
  //   setActiveStep((prevActiveStep) => prevActiveStep - 1);
  // };

  // const handleSkip = () => {
  //   if (!isStepOptional(activeStep)) {
  //     throw new Error("You can't skip a step that isn't optional.");
  //   }

  //   setActiveStep((prevActiveStep) => prevActiveStep + 1);
  //   setSkipped((prevSkipped) => {
  //     const newSkipped = new Set(prevSkipped.values());
  //     newSkipped.add(activeStep);
  //     return newSkipped;
  //   });
  // };


  // const onCodeEditorChange = (value: string, viewUpdate: ViewUpdate) => {
  //   let newSteps: Array<StepDefinition> = [...steps].map((stepDefinition: StepDefinition, index) => {
  //     if (index === activeStep) {
  //       stepDefinition.value = value;
  //     }
  //     return stepDefinition;
  //   });
  //   setSteps(newSteps);
  // };

  // const onNewJobActionClick = function () {
  //   setSteps(previous => {
  //     let numberOfCurrentJobs = steps.length - 2;
  //     let newJobName = `Job ${numberOfCurrentJobs + 1}`;
  //     let newJob: StepDefinition = {
  //       name: newJobName, label: newJobName, value: "", requiredCodeBlock: true, codeLanguges: [javascript({ jsx: true })]
  //     };
  //     previous.splice(steps.length - 1, 0, newJob);
  //     return [...previous];
  //   })
  // }

  const breadcrumbs = [
    <LinkBreadcrumd underline="hover" key="1" color="inherit" href="/actions">Actions</LinkBreadcrumd>,
    <Typography key="3" color="text.primary">new</Typography>
  ]

  return (
    <Stack spacing={4}>
      <Breadcrumbs breadcrumbs={breadcrumbs} />
      <PageEntityToReactNodes pageEntity={}/>

      {/* <Box sx={{ width: '100%' }} >
        <Stack spacing={4}>
          <Stepper activeStep={activeStep} alternativeLabel>
            {steps.map((stepDefinition, index) => {
              const stepProps: { completed?: boolean } = {};
              const labelProps: {
                optional?: React.ReactNode;
              } = {};
              if (isStepOptional(index)) {
                labelProps.optional = (
                  <Typography variant="caption">Optional</Typography>
                );
              }
              if (isStepSkipped(index)) {
                stepProps.completed = false;
              }
              return (
                <Step key={stepDefinition.name} {...stepProps}>
                  <StepLabel {...labelProps}>{stepDefinition.label}</StepLabel>
                </Step>
              );
            })}
          </Stepper>
          {activeStep === steps.length ? (
            <React.Fragment>
              <Typography sx={{ mt: 2, mb: 1 }}>
                All steps completed - you&apos;re finished
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'row', pt: 2 }}>
              </Box>

            </React.Fragment>
          ) : (
            <React.Fragment>
              <Box sx={{ px: '100px' }}>
                {steps
                  .filter((_, index) => index === activeStep)
                  .filter(step => step.requiredCodeBlock)
                  .map((stepDefinition) => {
                    return (
                      <Grid key={stepDefinition.name} alignItems="center" container spacing={3} >
                        <Grid item xs={4}>
                          <TextField
                            required sx={{ width: '100%' }}
                            label="Job name"
                            name={stepDefinition.name}
                            value={stepDefinition.label}
                            onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                              let newSteps: Array<StepDefinition> = [...steps].map((stepDefinition: StepDefinition, index) => {
                                if (index === activeStep) {
                                  stepDefinition.label = event.target.value;
                                }
                                return stepDefinition;
                              });
                              setSteps(newSteps)
                            }}
                            variant="standard" />
                        </Grid>
                        <Grid item xs={12}>
                          <CodeEditor
                            key={stepDefinition.name}
                            value={stepDefinition.value}
                            onChange={onCodeEditorChange}
                            language={[javascript({ jsx: true })]} />
                        </Grid>
                      </Grid>
                    )
                  })}
              </Box>

              <Box sx={{ display: 'flex', flexDirection: 'row', pt: 2, px: "100px" }}>
                <Button
                  color="inherit"
                  disabled={activeStep === 0}
                  onClick={handleBack}
                  sx={{ mr: 1 }}
                >
                  Back
                </Button>
                <Box sx={{ flex: '1 1 auto' }} />
                {isStepOptional(activeStep) && (
                  <Button color="inherit" onClick={handleSkip} sx={{ mr: 1 }}>
                    Skip
                  </Button>
                )}
                <Button onClick={handleNext}>
                  {activeStep === steps.length - 1 ? 'Finish' : 'Next'}
                </Button>
              </Box>
            </React.Fragment>
          )}
        </Stack>
      </Box> */}



      <FloatingSpeedDialButtons actions={actions} />
    </Stack>
  );
}