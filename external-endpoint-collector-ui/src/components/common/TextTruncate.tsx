import { Tooltip } from '@mui/material';
import React from 'react';

function truncate(text: string, maxTextLength: number) {
    return text.length >= maxTextLength ? text.substring(0, maxTextLength) + "..." : text;
}

export default function TextTruncate(props: any) {
    if (!props.text) {
        return (<span></span>);
    }
    return (
        (<Tooltip title={props.text.length >= props.maxTextLength ? props.text : ""}>
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {truncate(props.text, props.maxTextLength)}
            </span>
        </Tooltip>))
}