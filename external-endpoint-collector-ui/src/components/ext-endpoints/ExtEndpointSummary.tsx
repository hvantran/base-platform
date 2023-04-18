
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteIcon from '@mui/icons-material/Delete';
import ReadMoreIcon from '@mui/icons-material/ReadMore';
import RefreshIcon from '@mui/icons-material/Refresh';

import { Stack } from '@mui/material';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import { green, red } from '@mui/material/colors';
import React from 'react';
import {
  ColumnMetadata,
  PageEntityMetadata,
  PagingOptionMetadata,
  PagingResult,
  RestClient,
  SnackbarAlertMetadata,
  SnackbarMessage,
  SpeedDialActionMetadata,
  TableMetadata,
  WithLink
} from '../GenericConstants';
import ProcessTracking from '../common/ProcessTracking';

import { useNavigate } from 'react-router-dom';
import { EXT_ENDPOINT_BACKEND_URL, ExtEndpointOverview, ROOT_BREADCRUMB } from '../AppConstants';
import SnackbarAlert from '../common/SnackbarAlert';
import PageEntityRender from '../renders/PageEntityRender';



export default function ExtEndpointSummary() {
  const navigate = useNavigate();
  const [processTracking, setCircleProcessOpen] = React.useState(false);
  let initialPagingResult: PagingResult = { totalElements: 0, content: [] };
  const [pagingResult, setPagingResult] = React.useState(initialPagingResult);
  const [openError, setOpenError] = React.useState(false);
  const [openSuccess, setOpenSuccess] = React.useState(false);
  const [pageIndex, setPageIndex] = React.useState(0);
  const [pageSize, setPageSize] = React.useState(10);
  const [messageInfo, setMessageInfo] = React.useState<SnackbarMessage | undefined>(undefined);
  const restClient = new RestClient(setCircleProcessOpen, setMessageInfo, setOpenError, setOpenSuccess);

  const breadcrumbs = [
    <Link underline="hover" key="1" color="inherit" href='#'>
      {ROOT_BREADCRUMB}
    </Link>,
    <Typography key="3" color="text.primary">
      Summary
    </Typography>
  ];

  const columns: ColumnMetadata[] = [
    { id: 'endpointId', label: 'Endpoint ID', isHidden: true, minWidth: 100, isKeyColumn: true },
    { id: 'application', label: 'Application', minWidth: 100 },
    { id: 'taskName', label: 'Task', minWidth: 100 },
    {
      id: 'noAttemptTimes',
      label: 'Attempt times',
      minWidth: 100,
      align: 'left',
      format: (value: number) => value.toLocaleString('en-US'),
    },
    {
      id: 'noParallelThread',
      label: 'No parallel threads',
      minWidth: 170,
      align: 'left',
      format: (value: number) => value.toLocaleString('en-US'),
    },
    {
      id: 'extEndpoint',
      label: 'Target URL',
      minWidth: 170,
      align: 'left',
      format: (value: number) => value,
    },
    {
      id: 'extEndpointMethod',
      label: 'Method',
      minWidth: 100,
      align: 'left',
      format: (value: number) => value,
    },
    {
      id: 'extEndpointData',
      label: 'Data',
      minWidth: 170,
      align: 'left',
      format: (value: number) => value,
    },
    {
      id: 'successCriteria',
      label: 'Success Criteria',
      minWidth: 150,
      align: 'left',
      format: (value: string) => value,
    },
    {
      id: 'actions',
      label: '',
      minWidth: 100,
      align: 'right',
      actions: [
        {
          actionIcon: <ReadMoreIcon />,
          actionLabel: "Action details",
          actionName: "gotoActionDetail",
          onClick: (row: ExtEndpointOverview) => {
            return () => navigate(`/endpoints/${row.endpointId}`)
          }
        },
        {
          actionIcon: <DeleteIcon />,
          properties: { sx: { color: red[800] } },
          actionLabel: "Delete endpoint",
          actionName: "deletection",
          onClick: (row: ExtEndpointOverview) => {
            return () => deleteEndpointCollector(row.endpointId)
          }
        }
      ]
    }
  ];


  const deleteEndpointCollector = async (endpointId: number) => {

    const requestOptions = {
      method: "DELETE",
      headers: {
        "Accept": "application/json"
      }
    }
    const targetURL = `${EXT_ENDPOINT_BACKEND_URL}/${endpointId}`;
    await restClient.sendRequest(requestOptions, targetURL, () => {
      loadEndpointSummaryAsync(pagingOptions.pageIndex, pagingOptions.pageSize);
      return undefined;
    }, async (response: Response) => {
      let responseJSON = await response.json();
      return { 'message': responseJSON['message'], key: new Date().getTime() } as SnackbarMessage;
    });
  }

  const loadEndpointSummaryAsync = async (pageIndex: number, pageSize: number) => {
    const requestOptions = {
      method: "GET",
      headers: {
        "Accept": "application/json"
      }
    }

    const targetURL = `${EXT_ENDPOINT_BACKEND_URL}?pageIndex=${pageIndex}&pageSize=${pageSize}`;
    await restClient.sendRequest(requestOptions, targetURL, async (response) => {
      let extEndpointPagingResult = await response.json() as PagingResult;
      extEndpointPagingResult.elementTransformCallback = (record) => {
        const transfromRecord: ExtEndpointOverview = {
          endpointId: record.endpointId,
          application: record.input.application,
          taskName: record.input.taskName,
          noAttemptTimes: record.input.noAttemptTimes,
          noParallelThread: record.input.noParallelThread,
          extEndpoint: record.input.requestInfo.extEndpoint,
          extEndpointMethod: record.input.requestInfo.method,
          extEndpointData: record.input.requestInfo.data,
          successCriteria: record.filter.successCriteria
        }
        return transfromRecord;
      }
      setPagingResult(extEndpointPagingResult);
      return { 'message': 'Load endpoints successfully!!', key: new Date().getTime() } as SnackbarMessage;
    }, async (response: Response) => {
      let responseJSON = await response.json();
      return { 'message': responseJSON['message'], key: new Date().getTime() } as SnackbarMessage;
    });
  }

  React.useEffect(() => {
    loadEndpointSummaryAsync(pageIndex, pageSize);
  }, [pageIndex, pageSize])

  const endpoints: Array<SpeedDialActionMetadata> = [
    {
      actionIcon: WithLink('/endpoints/new', <AddCircleOutlineIcon />), actionName: 'create', actionLabel: 'New Endpoint', properties: {
        sx: {
          bgcolor: green[500],
          '&:hover': {
            bgcolor: green[800],
          }
        }
      }
    }
  ];

  let pagingOptions: PagingOptionMetadata = {
    pageIndex,
    pageSize,
    component: 'div',
    rowsPerPageOptions: [5, 10, 20],
    onPageChange: (pageIndex: number, pageSize: number) => {
      setPageIndex(pageIndex);
      setPageSize(pageSize);
    }
  }

  let tableMetadata: TableMetadata = {
    columns,
    pagingOptions: pagingOptions,
    pagingResult: pagingResult
  }

  let pageEntityMetadata: PageEntityMetadata = {
    pageName: 'ext-endpoint-summary',
    floatingActions: endpoints,
    tableMetadata: tableMetadata,
    breadcumbsMeta: breadcrumbs,
    pageEntityActions: [
      {
        actionIcon: <RefreshIcon />,
        actionLabel: "Refresh endpoints",
        actionName: "refreshAction",
        onClick: () => () => loadEndpointSummaryAsync(pageIndex, pageSize)
      }
    ]
  }

  let snackbarAlertMetadata: SnackbarAlertMetadata = {
    openError,
    openSuccess,
    setOpenError,
    setOpenSuccess,
    messageInfo
  }

  return (
    <Stack spacing={2}>
      <PageEntityRender {...pageEntityMetadata}></PageEntityRender>
      <ProcessTracking isLoading={processTracking}></ProcessTracking>
      <SnackbarAlert {...snackbarAlertMetadata}></SnackbarAlert>
    </Stack>
  );
}