
import ReadMoreIcon from '@mui/icons-material/ReadMore';
import { Stack } from '@mui/material';
import React from 'react';
import {
    ColumnMetadata, PageEntityMetadata, PagingOptionMetadata,
    PagingResult, TableMetadata
} from '../GenericConstants';

import { useNavigate } from 'react-router-dom';
import { JobOverview, JOB_MANAGER_API_URL } from '../AppConstants';
import JobStatus from '../common/JobStatus';
import PageEntityRender from '../renders/PageEntityRender';
import TextTruncate from '../common/TextTruncate';


export default function ActionJobTable(props: any) {

    const navigate = useNavigate();
    const targetAction = props.actionId
    const setCircleProcessOpen = props.setCircleProcessOpen
    let initialPagingResult: PagingResult = { totalElements: 0, content: []};
    const [pagingResult, setPagingResult] = React.useState(initialPagingResult);

    const columns: ColumnMetadata[] = [
        { id: 'hash', label: 'Hash', minWidth: 100, isHidden: true, isKeyColumn: true },
        { id: 'name', label: 'Name', minWidth: 100 },
        {
            id: 'state',
            label: 'State',
            minWidth: 100,
            align: 'right',
            format: (value: number) => value.toLocaleString('en-US'),
        },
        {
            id: 'status',
            label: 'Status',
            minWidth: 100,
            align: 'right',
            format: (value: string) => (<JobStatus status={value}/>)
        },
        {
            id: 'startedAt',
            label: 'Started At',
            minWidth: 100,
            align: 'right',
            format: (value: number) => {
                if (!value) {
                    return "";
                }

                let createdAtDate = new Date(0);
                createdAtDate.setUTCSeconds(value);
                return createdAtDate.toISOString();
            }
        },
        {
            id: 'elapsedTime',
            label: 'Elapsed Time',
            minWidth: 100,
            align: 'right',
            format: (value: number) => value.toLocaleString('en-US')
        },
        {
            id: 'failureNotes',
            label: 'Failure Notes',
            minWidth: 200,
            align: 'left',
            format: (value: string) => (<TextTruncate text={value} maxTextLength={100}/>)
        },
        {
            id: 'actions',
            label: '',
            align: 'right',
            actions: [{
                actionIcon: <ReadMoreIcon />,
                actionLabel: "Job details",
                actionName: "gotoJobDetail",
                onClick: (row: JobOverview) => {
                    return () => navigate(`/jobs/${row.hash}`)
                }
            }]
        }
    ];

    const fetchJobsAsync = async (pageIndex: number, pageSize: number) => {
        const requestOptions = {
            method: "GET",
            headers: {
                "Accept": "application/json"
            }
        }

        try {
            setCircleProcessOpen(true);
            let response = await fetch(`${JOB_MANAGER_API_URL}?actionId=${encodeURIComponent(targetAction)}&pageIndex=${encodeURIComponent(pageIndex)}&pageSize=${encodeURIComponent(pageSize)}`, requestOptions);
            let actionPagingResult = await response.json() as PagingResult;
            setPagingResult(actionPagingResult);
        } finally {
            setCircleProcessOpen(false);
        }
    }

    React.useEffect(() => {
        fetchJobsAsync(pagingOptions.pageIndex, pagingOptions.pageSize);
    }, [])

    let pagingOptions: PagingOptionMetadata = {
        pageIndex: 0,
        component: 'div',
        pageSize: 10,
        rowsPerPageOptions: [5, 10, 20],
        onPageChange: (pageIndex: number, pageSize: number) => {
            fetchJobsAsync(pageIndex, pageSize);
        }
    }

    let tableMetadata: TableMetadata = {
        columns,
        pagingOptions: pagingOptions,
        pagingResult: pagingResult
    }

    let pageEntityMetadata: PageEntityMetadata = {
        pageName: 'action-job-summary',
        tableMetadata: tableMetadata
    }

    return (
        <Stack spacing={2}>
            <PageEntityRender {...pageEntityMetadata}></PageEntityRender>
        </Stack>
    );
}