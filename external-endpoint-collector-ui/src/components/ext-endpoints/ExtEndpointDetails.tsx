
import ReadMoreIcon from '@mui/icons-material/ReadMore';
import RefreshIcon from '@mui/icons-material/Refresh';

import { Stack } from '@mui/material';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import React from 'react';
import {
  ColumnMetadata,
  PageEntityMetadata,
  PagingOptionMetadata,
  PagingResult,
  RestClient,
  SnackbarAlertMetadata,
  SnackbarMessage,
  TableMetadata
} from '../GenericConstants';
import ProcessTracking from '../common/ProcessTracking';

import { useNavigate, useParams } from 'react-router-dom';
import { EXT_ENDPOINT_BACKEND_URL, ExtEndpointResponseOverview, ROOT_BREADCRUMB } from '../AppConstants';
import SnackbarAlert from '../common/SnackbarAlert';
import PageEntityRender from '../renders/PageEntityRender';



export default function ExtEndpointDetails() {
  const targetAction = useParams();
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

  const application: string | undefined = targetAction.application;
  if (!application) {
    throw new Error("Action is required");
  }

  const breadcrumbs = [
    <Link underline="hover" key="1" color="inherit" href='/endpoints'>
      {ROOT_BREADCRUMB}
    </Link>,
    <Typography key="3" color="text.primary">
      {application}
    </Typography>
  ];

  const columns: ColumnMetadata[] = [
    {
      id: 'column1',
      label: 'Column 1',
      minWidth: 100,
      isKeyColumn: true
    },
    {
      id: 'column2',
      label: 'Column 2',
    },
    {
      id: 'column3',
      label: 'Column 3',
      align: 'left',
      format: (value: string) => value
    },
    {
      id: 'column4',
      label: 'Column 4',
      align: 'left',
      format: (value: string) => value
    },
    {
      id: 'column5',
      label: 'Column 5',
      align: 'left',
      format: (value: string) => value,
    },
    {
      id: 'column6',
      label: 'Column 6',
      align: 'left',
      format: (value: string) => value,
    },
    {
      id: 'column7',
      label: 'Column 7',
      align: 'left',
      format: (value: string) => value,
    },
    {
      id: 'column8',
      label: 'Column 8',
      align: 'left',
      format: (value: string) => value,
    },
    {
      id: 'column9',
      label: 'Column 9',
      align: 'left',
      format: (value: string) => value,
    },
    {
      id: 'column10',
      label: 'Column 10',
      align: 'left',
      format: (value: string) => value,
    },
    {
      id: 'actions',
      label: '',
      align: 'right',
      actions: [
        {
          actionIcon: <ReadMoreIcon />,
          actionLabel: "Response details",
          actionName: "gotoActionDetail",
          onClick: (row: ExtEndpointResponseOverview) => {
            return () => navigate(`/endpoints/${application}/response/${row.id}`)
          }
        }
      ]
    }
  ];

  const loadExternalEndpointResponseAsync = async (pageIndex: number, pageSize: number) => {
    const requestOptions = {
      method: "GET",
      headers: {
        "Accept": "application/json"
      }
    }

    const targetURL = `${EXT_ENDPOINT_BACKEND_URL}/${application}/responses?pageIndex=${pageIndex}&pageSize=${pageSize}`;
    await restClient.sendRequest(requestOptions, targetURL, async (response) => {
      let extEndpointPagingResult = await response.json() as PagingResult;
      setPagingResult(extEndpointPagingResult);
      return { 'message': 'Load endpoint responses successfully!!', key: new Date().getTime() } as SnackbarMessage;
    }, async (response: Response) => {
      let responseJSON = await response.json();
      return { 'message': responseJSON['message'], key: new Date().getTime() } as SnackbarMessage;
    });
  }

  React.useEffect(() => {
    loadExternalEndpointResponseAsync(pageIndex, pageSize);
  }, [pageIndex, pageSize])


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
    pageName: 'ext-endpoint-responses-summary',
    tableMetadata: tableMetadata,
    breadcumbsMeta: breadcrumbs,
    pageEntityActions: [
      {
        actionIcon: <RefreshIcon />,
        actionLabel: "Refresh",
        actionName: "refreshAction",
        onClick: () => () => loadExternalEndpointResponseAsync(pageIndex, pageSize)
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