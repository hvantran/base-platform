
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteIcon from '@mui/icons-material/Delete';
import FavoriteIcon from '@mui/icons-material/GradeOutlined';
import ReadMoreIcon from '@mui/icons-material/ReadMore';
import { Stack } from '@mui/material';
import { green, red, yellow } from '@mui/material/colors';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import React from 'react';
import Breadcrumbs from '../common/Breadcrumbs';
import ProcessTracking from '../common/ProcessTracking';
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

import { useNavigate } from 'react-router-dom';
import { ActionOverview, ACTION_MANAGER_API_URL } from '../AppConstants';
import SnackbarAlert from '../common/SnackbarAlert';
import PageEntityRender from '../renders/PageEntityRender';



export default function ActionSummary() {
  const navigate = useNavigate();
  const [processTracking, setCircleProcessOpen] = React.useState(false);
  let initialPagingResult: PagingResult = { totalElements: 0, content: [] };
  const [pagingResult, setPagingResult] = React.useState(initialPagingResult);
  const [openError, setOpenError] = React.useState(false);
  const [openSuccess, setOpenSuccess] = React.useState(false);
  const [messageInfo, setMessageInfo] = React.useState<SnackbarMessage | undefined>(undefined);
  const restClient = new RestClient(setCircleProcessOpen, setMessageInfo, setOpenError, setOpenSuccess);

  const breadcrumbs = [
    <Link underline="hover" key="1" color="inherit" href='#'>
      Actions
    </Link>,
    <Typography key="3" color="text.primary">
      Summary
    </Typography>
  ];

  const columns: ColumnMetadata[] = [
    { id: 'hash', label: 'Hash', minWidth: 100, isHidden: true, isKeyColumn: true },
    { id: 'name', label: 'Name', minWidth: 100 },
    {
      id: 'numberOfJobs',
      label: 'Total Jobs',
      minWidth: 170,
      align: 'right',
      format: (value: number) => value.toLocaleString('en-US'),
    },
    {
      id: 'numberOfFailureJobs',
      label: 'Number of failure Jobs',
      minWidth: 170,
      align: 'right',
      format: (value: number) => value.toLocaleString('en-US'),
    },
    {
      id: 'numberOfSuccessJobs',
      label: 'Number of success Jobs',
      minWidth: 170,
      align: 'right',
      format: (value: number) => value.toFixed(2),
    },
    {
      id: 'createdAt',
      label: 'Created at',
      minWidth: 170,
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
      id: 'actions',
      label: '',
      align: 'right',
      actions: [
        {
          actionIcon: <ReadMoreIcon />,
          actionLabel: "Action details",
          actionName: "gotoActionDetail",
          onClick: (row: ActionOverview) => {
            return () => navigate(`/actions/${row.hash}`)
          }
        },
        {
          actionIcon: <DeleteIcon />,
          properties: {sx:{color: red[800]}},
          actionLabel: "Delete action",
          actionName: "deleteAction",
          onClick: (row: ActionOverview) => {
            return () => deleteAction(row.hash);
          }
        },
        {
          actionIcon: <FavoriteIcon />,
          properties: {sx:{color: yellow[800]}},
          actionLabel: "Favorite action",
          actionName: "favoriteAction",
          onClick: (row: ActionOverview) => {
            return () => favoriteAction(row.hash);
          }
        },
      ]
    }
  ];

  const favoriteAction =async (actionId: string) => {
  }

  const deleteAction =async (actionId: string) => {

    const requestOptions = {
      method: "DELETE",
      headers: {
        "Accept": "application/json"
      }
    }
    const targetURL = `${ACTION_MANAGER_API_URL}/${actionId}`;
    await restClient.sendRequest(requestOptions, targetURL, async () => {
      return { 'message': 'Delete action successfully!!', key: new Date().getTime() } as SnackbarMessage;
    }, async (response: Response) => {
      let responseJSON = await response.json();
      return { 'message': responseJSON['message'], key: new Date().getTime() } as SnackbarMessage;
    });

    loadActionsAsync(pagingOptions.pageIndex, pagingOptions.pageSize);
  }

  const loadActionsAsync = async (pageIndex: number, pageSize: number) => {
    const requestOptions = {
      method: "GET",
      headers: {
        "Accept": "application/json"
      }
    }

    const targetURL = `${ACTION_MANAGER_API_URL}?pageIndex=${pageIndex}&pageSize=${pageSize}`;
    await restClient.sendRequest(requestOptions, targetURL, async (response) => {
      let actionPagingResult = await response.json() as PagingResult;
      setPagingResult(actionPagingResult);
      return { 'message': 'Load actions successfully!!', key: new Date().getTime() } as SnackbarMessage;
    }, async (response: Response) => {
      let responseJSON = await response.json();
      return { 'message': responseJSON['message'], key: new Date().getTime() } as SnackbarMessage;
    });
  }

  React.useEffect(() => {
    loadActionsAsync(pagingOptions.pageIndex, pagingOptions.pageSize);
  }, [])

  const actions: Array<SpeedDialActionMetadata> = [
    {
      actionIcon: WithLink('/actions/new', <AddCircleOutlineIcon />), actionName: 'create', actionLabel: 'New Action', properties: {
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
    pageIndex: 0,
    component: 'div',
    pageSize: 10,
    rowsPerPageOptions: [5, 10, 20],
    onPageChange: (pageIndex: number, pageSize: number) => {
      loadActionsAsync(pageIndex, pageSize);
    }
  }

  let tableMetadata: TableMetadata = {
    columns,
    pagingOptions: pagingOptions,
    pagingResult: pagingResult
  }

  let pageEntityMetadata: PageEntityMetadata = {
    pageName: 'action-summary',
    floatingActions: actions,
    tableMetadata: tableMetadata
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
      <Breadcrumbs breadcrumbs={breadcrumbs} />
      <PageEntityRender {...pageEntityMetadata}></PageEntityRender>
      <ProcessTracking isLoading={processTracking}></ProcessTracking>
      <SnackbarAlert {...snackbarAlertMetadata}></SnackbarAlert>
    </Stack>
  );
}