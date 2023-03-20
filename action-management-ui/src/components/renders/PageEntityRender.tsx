import { Stack } from '@mui/material';
import * as React from 'react';
import { PageEntityMetadata } from '../GenericConstants';
import FloatingSpeedDialButtons from '../common/FloatingActions';
import StepperRender from './StepperRender';
import TableRender from './TableRender';

export default function PageRender(props: PageEntityMetadata) {
    let floatingActions = props.floatingActions
    let stepMetadatas = props.stepMetadatas
    let tableMetadata = props.tableMetadata
    let pageName = props.pageName

    let nodes: Array<React.ReactNode> = []
    if (floatingActions) {
        nodes.push(<FloatingSpeedDialButtons key={pageName + '-floating-actions'} actions={floatingActions} />)
    }

    if (stepMetadatas) {
        nodes.push(<StepperRender key={pageName + '-step-render'} initialStepMetadata={stepMetadatas} />)
    }

    if (tableMetadata) {
        nodes.push(<TableRender key={pageName + '-table'} {...tableMetadata} />)
    }

    return (
        <Stack spacing={1}>
            {nodes}
        </Stack>
    )
}
