import * as React from 'react';
import Breadcrumbs from '@mui/material/Breadcrumbs';
import Typography from '@mui/material/Typography';
import Link from '@mui/material/Link';
import Stack from '@mui/material/Stack';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';


export default function CustomSeparator(props: any) {
    const breadcrumbs1 = props.breadcrumbs

    return (
        <Breadcrumbs
            separator={<NavigateNextIcon fontSize="small" />}
            aria-label="breadcrumb"
        >
            {breadcrumbs1}
        </Breadcrumbs>
    );
}