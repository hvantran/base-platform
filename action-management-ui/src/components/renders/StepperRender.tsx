
import DoneAllIcon from '@mui/icons-material/DoneAll';
import NavigateBeforeIcon from '@mui/icons-material/NavigateBeforeRounded';
import NavigateNextIcon from '@mui/icons-material/NavigateNextRounded';
import { IconButton, Stack, Tooltip } from '@mui/material';
import Box from '@mui/material/Box';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Stepper from '@mui/material/Stepper';
import Typography from '@mui/material/Typography';
import * as React from 'react';
import { StepMetadata } from '../GenericConstants';
import PropertyRender from './PropertyRender';

export default function StepperRender(props: any) {
    let initialStepMetadata: Array<StepMetadata> = props.initialStepMetadata;
    const [activeStep, setActiveStep] = React.useState(0);

    const isStepOptional = (stepMetadata: StepMetadata) => {
        return stepMetadata.isOptional;
    };

    const handleNext = () => {
        setActiveStep((prevActiveStep) => prevActiveStep + 1);
    };

    const handleBack = () => {
        setActiveStep((prevActiveStep) => prevActiveStep - 1);
    };

    const handleFinish = () => {
        let onFinishStepClick = initialStepMetadata[activeStep].onFinishStepClick
        if (onFinishStepClick) {
            onFinishStepClick(initialStepMetadata);
        }
    }


    return (
        <Box sx={{ width: '100%' }} >
            <Stack spacing={4}>
                <Stepper activeStep={activeStep} alternativeLabel>
                    {initialStepMetadata.map((stepMetadata, index) => {
                        const stepProps: { completed?: boolean } = {};
                        const labelProps: { optional?: React.ReactNode } = {};

                        if (isStepOptional(stepMetadata)) {
                            labelProps.optional = (<Typography variant="caption">Optional</Typography>);
                        }
                        return (
                            <Step key={stepMetadata.name} {...stepProps}>
                                <StepLabel {...labelProps}>{stepMetadata.label}</StepLabel>
                            </Step>
                        );
                    })}
                </Stepper>
                    <React.Fragment>
                        <Box sx={{ display: 'flex', flexDirection: 'row', pt: 2, px: "100px" }}>
                            <IconButton
                                disabled={activeStep === 0}
                                sx={{ mr: 1 }}
                                color="inherit"
                                onClick={handleBack}
                                aria-label="Next"
                                component="label">
                                <Tooltip title="Back">
                                    <NavigateBeforeIcon />
                                </Tooltip>
                            </IconButton>
                            <Box sx={{ flex: '1 1 auto' }} />
                            {
                                activeStep !== initialStepMetadata.length - 1 ?
                                    (<IconButton onClick={handleNext} color="primary" aria-label="Next" component="label">
                                        <Tooltip title="Next">
                                            <NavigateNextIcon/>
                                        </Tooltip>
                                    </IconButton>) 
                                    :
                                    (<IconButton onClick={handleFinish} color="primary" aria-label="Next" component="label">
                                        <Tooltip title="Finish">
                                            <DoneAllIcon />
                                        </Tooltip>
                                    </IconButton>)
                            }
                        </Box>

                        {/* Render the Stepper properties */}
                        <Box sx={{ px: '100px' }}>
                            {initialStepMetadata
                                .filter((_, index) => index === activeStep)
                                .flatMap((stepDefinition) => stepDefinition.properties)
                                .flatMap((propertyMeta) => {
                                    return (<PropertyRender key={propertyMeta.propName} property={propertyMeta} />)
                                })}
                        </Box>
                    </React.Fragment>
            </Stack>
        </Box >
    )
}