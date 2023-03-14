
import { Stack } from '@mui/material';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import { DataGrid, GridColDef, GridValueGetterParams } from '@mui/x-data-grid';
import React from 'react';
import Breadcrumbs from '../components/Breadcrumbs';

import ActionContentControlButtons from './ActionContentControlButtons';

const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 70 },
    { field: 'firstName', headerName: 'First name', width: 130 },
    { field: 'lastName', headerName: 'Last name', width: 130 },
    {
        field: 'age',
        headerName: 'Age',
        type: 'number',
        width: 90,
    },
    {
        field: 'fullName',
        headerName: 'Full name',
        description: 'This column has a value getter and is not sortable.',
        sortable: false,
        width: 160,
        valueGetter: (params: GridValueGetterParams) =>
            `${params.row.firstName || ''} ${params.row.lastName || ''}`,
    },
];

const rows = [
    { id: 1, lastName: 'Snow', firstName: 'Jon', age: 35 },
    { id: 2, lastName: 'Lannister', firstName: 'Cersei', age: 42 },
    { id: 3, lastName: 'Lannister', firstName: 'Jaime', age: 45 },
    { id: 4, lastName: 'Stark', firstName: 'Arya', age: 16 },
    { id: 5, lastName: 'Targaryen', firstName: 'Daenerys', age: null },
    { id: 6, lastName: 'Melisandre', firstName: null, age: 150 },
    { id: 7, lastName: 'Clifford', firstName: 'Ferrara', age: 44 },
    { id: 8, lastName: 'Frances', firstName: 'Rossini', age: 36 },
    { id: 9, lastName: 'Roxie', firstName: 'Harvey', age: 65 },
];


const breadcrumbs = [
    <Link underline="hover" key="1" color="inherit" href="/actions">
      Actions
    </Link>,
    <Link
      underline="hover"
      key="2"
      color="inherit"
      href="/material-ui/getting-started/installation/"
    >
      Summary
    </Link>,
    <Typography key="3" color="text.primary">
      --Action details
    </Typography>,
  ];

const dynamicHeight = Math.min(rows.length * 6 + 10, 80) + 'vh'

export default function ActionContent() {
    return (
        <Stack spacing={4}>
            <Breadcrumbs breadcrumbs={breadcrumbs} />
            <div style={{ height: dynamicHeight, width: '100%' }}>
                <DataGrid
                    rows={rows}
                    columns={columns}
                    pageSizeOptions={[1, 2]}
                    checkboxSelection
                />
            </div>
            <ActionContentControlButtons />
        </Stack >
    );
}