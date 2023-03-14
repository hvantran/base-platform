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
import withLink, { PageEntityMetadata, PropertyMetadata, PropType, StepMetadata } from './AppConstants';
import Breadcrumbs from './Breadcrumbs';
import CodeEditor from './CodeEditor';
import FloatingSpeedDialButtons from './FloatingActions';

export function PageEntityToReactNodes(props: any) {
    let pageEntity: PageEntityMetadata = props.pageEntity

    let floatingActions = pageEntity.floatingActions
    let stepMetadatas = pageEntity.stepMetadatas
    let templateStepMetadata = pageEntity.stepMetadatas
    let nodes: Array<React.ReactNode> = []
    if (floatingActions) {
        nodes.push(<FloatingSpeedDialButtons actions={floatingActions} />)
    }

    if (stepMetadatas) {
        nodes.push(<StepRenderToReactNodes initialStepMetadata={stepMetadatas} />)

    }

    return (
        <Box>
        {nodes}
        </Box>
    )
}

export function ProppertyToReactNode(props: any) {
    let property: PropertyMetadata = props.property
    switch (property.propType) {
            
        case PropType.CodeEditor:
            if (!property.codeEditorMeta) {
                throw new Error(`codeEditorMeta is required for ${property.propName} code editor property`);
            }
            let codeEditorMeta = property.codeEditorMeta
            return (
                <CodeEditor
                    key={property.propName}
                    value={property.propName}
                    onChange={codeEditorMeta.onChangeEvent}
                    language={codeEditorMeta.codeLanguges} />
            )

        case PropType.InputText:
        default:
            if (!property.textFieldMeta) {
                throw new Error(`textFieldMeta is required for ${property.propName} text property`);
            }
            let textFieldMeta = property.textFieldMeta
            return (
                <TextField
                    required sx={{ width: '100%' }}
                    key={property.propName}
                    label={property.propLabel}
                    name={property.propName}
                    value={property.propLabel}
                    onChange={textFieldMeta.onChangeEvent}
                    variant="standard" />
            )
    }
}

export function StepRenderToReactNodes(props: any) {
    let initialStepMetadata: Array<StepMetadata> = props.initialStepMetadata;
    let [stepMetadatas, setStepMetadata] = React.useState(initialStepMetadata);
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


    return (
        <Box sx={{ width: '100%' }} >
            <Stack spacing={4}>
                <Stepper activeStep={activeStep} alternativeLabel>
                    {stepMetadatas.map((stepMetadata, index) => {
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
                {activeStep === stepMetadatas.length ? (
                    <React.Fragment>
                        <Typography sx={{ mt: 2, mb: 1 }}>
                            All steps completed - you&apos;re finished
                        </Typography>
                        {/* This box is used to review all steps*/}
                        <Box sx={{ display: 'flex', flexDirection: 'row', pt: 2 }}></Box>
                    </React.Fragment>
                ) : (
                    <React.Fragment>
                        <Box sx={{ px: '100px' }}>
                            {stepMetadatas
                                .filter((_, index) => index === activeStep)
                                .flatMap((stepDefinition) => stepDefinition.properties)
                                .flatMap((propertyMeta) => {
                                    return (<ProppertyToReactNode property={propertyMeta}/>)
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
                            <Button onClick={handleNext}>
                                {activeStep === stepMetadatas.length - 1 ? 'Finish' : 'Next'}
                            </Button>
                        </Box>
                    </React.Fragment>
                )}
            </Stack>
        </Box>
    )
}