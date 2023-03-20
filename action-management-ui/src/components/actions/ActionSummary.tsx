
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import FavoriteIcon from '@mui/icons-material/GradeOutlined';
import ReadMoreIcon from '@mui/icons-material/ReadMore';
import { Stack } from '@mui/material';
import { green, red, yellow } from '@mui/material/colors';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import React from 'react';
import {
  PagingOptionMetadata,
  PagingResult,
  SpeedDialActionMetadata,
  WithLink,
  ColumnMetadata, PageEntityMetadata, TableMetadata
} from '../GenericConstants';
import Breadcrumbs from '../common/Breadcrumbs';
import ProcessTracking from '../common/ProcessTracking';

import { useNavigate } from 'react-router-dom';
import PageEntityRender from '../renders/PageEntityRender';
import { ActionOverview, ACTION_MANAGER_API_URL } from '../AppConstants';



export default function ActionSummary() {
  const navigate = useNavigate();
  const [processTracking, setCircleProcessOpen] = React.useState(false);
  let initialPagingResult: PagingResult = { totalElements: 0, content: [] };
  const [pagingResult, setPagingResult] = React.useState(initialPagingResult);

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
      actions: [{
        actionIcon: <ReadMoreIcon />,
        actionLabel: "Action details",
        actionName: "gotoActionDetail",
        onClick: (row: ActionOverview) => {
          return () => navigate(`/actions/${row.hash}`)
        }
      }]
    }
  ];

  const fetchActionsAsync = async (pageIndex: number, pageSize: number) => {
    const requestOptions = {
      method: "GET",
      headers: {
        "Accept": "application/json"
      }
    }

    try {
      setCircleProcessOpen(true);
      let response = await fetch(`${ACTION_MANAGER_API_URL}?pageIndex=${pageIndex}&pageSize=${pageSize}`, requestOptions);
      let actionPagingResult = await response.json() as PagingResult;
      setPagingResult(actionPagingResult);
    } finally {
      setCircleProcessOpen(false);
    }
  }

  React.useEffect(() => {
    fetchActionsAsync(pagingOptions.pageIndex, pagingOptions.pageSize);
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
    },
    {
      actionIcon: <DeleteIcon />, actionName: 'delete', actionLabel: 'Delete Selected Actions', properties: {
        sx: {
          bgcolor: red[500],
          '&:hover': {
            bgcolor: red[800],
          }
        }
      }
    },
    {
      actionIcon: <FavoriteIcon />, actionName: 'favorite', actionLabel: 'Favorite', properties: {
        sx: {
          bgcolor: yellow[500],
          '&:hover': {
            bgcolor: yellow[800],
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
      fetchActionsAsync(pageIndex, pageSize);
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

  return (
    <Stack spacing={2}>
      <Breadcrumbs breadcrumbs={breadcrumbs} />
      <PageEntityRender {...pageEntityMetadata}></PageEntityRender>
      <ProcessTracking isLoading={processTracking}></ProcessTracking>
    </Stack>
  );
}