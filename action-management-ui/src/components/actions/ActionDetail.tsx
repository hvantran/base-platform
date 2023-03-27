
import { Stack } from '@mui/material';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import React from 'react';
import DeleteIcon from '@mui/icons-material/Delete';
import ReplayIcon from '@mui/icons-material/Replay';

import { useParams } from 'react-router-dom';
import { ActionDetails, ACTION_MANAGER_API_URL } from '../AppConstants';
import ProcessTracking from '../common/ProcessTracking';
import SnackbarAlert from '../common/SnackbarAlert';
import { DialogMetadata, PageEntityMetadata, PropType, RestClient, SnackbarAlertMetadata, SnackbarMessage } from '../GenericConstants';
import PageEntityRender from '../renders/PageEntityRender';
import ActionJobTable from './ActionJobTable';
import { red } from '@mui/material/colors';
import { useNavigate } from 'react-router-dom';
import ConfirmationDialog from '../common/ConfirmationDialog';


export default function ActionDetail() {

  const targetAction = useParams();
  const navigate = useNavigate();
  const initialActionDetailData: ActionDetails = {
    hash: '',
    name: '',
    numberOfSuccessJobs: 0,
    numberOfFailureJobs: 0,
    numberOfJobs: 0,
    createdAt: 0,
    configurations: '',
    description: ''
  };
  const actionId: string | undefined = targetAction.actionId;
  if (!actionId) {
    throw new Error("Action is required");
  }

  const [processTracking, setCircleProcessOpen] = React.useState(false);
  const [_, setActionDetailData] = React.useState(initialActionDetailData);
  const [openError, setOpenError] = React.useState(false);
  const [deleteConfirmationDialogOpen, setDeleteConfirmationDialogOpen] = React.useState(false);
  const [openSuccess, setOpenSuccess] = React.useState(false);
  const [replayFlag, setReplayActionFlag] = React.useState(false);
  const [messageInfo, setMessageInfo] = React.useState<SnackbarMessage | undefined>(undefined);
  const restClient = new RestClient(setCircleProcessOpen, setMessageInfo, setOpenError, setOpenSuccess);

  const loadActionDetailAsync = async () => {
    const requestOptions = {
      method: "GET",
      headers: {
        "Accept": "application/json"
      }
    }

    const targetURL = `${ACTION_MANAGER_API_URL}/${encodeURIComponent(actionId)}`;
    await restClient.sendRequest(requestOptions, targetURL, async (response) => {
      let actionDetailResult = await response.json() as ActionDetails;
      setActionDetailData(actionDetailResult);
      return undefined;
    }, async (response: Response) => {
      let responseJSON = await response.json();
      return { 'message': responseJSON['message'], key: new Date().getTime() } as SnackbarMessage;
    });
  }

  const deleteAction =async (actionId: string) => {

    const requestOptions = {
      method: "DELETE",
      headers: {
        "Accept": "application/json"
      }
    }
    const targetURL = `${ACTION_MANAGER_API_URL}/${actionId}`;
    await restClient.sendRequest(requestOptions, targetURL, () => {
      navigate("/actions");
      return undefined;
    }, async (response: Response) => {
      let responseJSON = await response.json();
      return { 'message': responseJSON['message'], key: new Date().getTime() } as SnackbarMessage;
    });
  }

  const replayAction =async (actionId: string) => {

    const requestOptions = {
      method: "GET",
      headers: {
        "Accept": "application/json"
      }
    }
    const targetURL = `${ACTION_MANAGER_API_URL}/${actionId}/replay`;
    await restClient.sendRequest(requestOptions, targetURL, async () => {
      setReplayActionFlag(previous => !previous);
      return undefined
    }, async (response: Response) => {
      let responseJSON = await response.json();
      return { 'message': responseJSON['message'], key: new Date().getTime() } as SnackbarMessage;
    });
  }

  React.useEffect(() => {
    loadActionDetailAsync();
  }, []);

  let pageEntityMetadata: PageEntityMetadata = {
    pageName: 'action-details',
    breadcumbsMeta: [
      <Link underline="hover" key="1" color="inherit" href="/actions">Actions</Link>,
      <Typography key="3" color="text.primary">{actionId}</Typography>,
    ],
    pageEntityActions: [
      {
        actionIcon: <ReplayIcon />,
        actionLabel: "Replay action",
        actionName: "replayAction",
        onClick: () => () => replayAction(actionId)
      },
      {
        actionIcon: <DeleteIcon />,
        properties: {sx:{color: red[800]}},
        actionLabel: "Delete action",
        actionName: "deleteAction",
        onClick: () => () => setDeleteConfirmationDialogOpen(true)
      }
    ],
    properties: [
    ]
  }


  let snackbarAlertMetadata: SnackbarAlertMetadata = {
    openError,
    openSuccess,
    setOpenError,
    setOpenSuccess,
    messageInfo
  }

  let actionJobTableParams = {
    setCircleProcessOpen,
    setMessageInfo,
    setOpenError,
    setOpenSuccess,
    replayFlag
  }

  let confirmationDeleteDialogMeta: DialogMetadata = {
    open: deleteConfirmationDialogOpen,
    title: "Delete Action",
    content: "Are you sure you want to delete this action?",
    positiveText: "Yes",
    negativeText: "No",
    negativeAction() {
      setDeleteConfirmationDialogOpen(false);
    },
    positiveAction() {
      deleteAction(actionId);
    },
  }


  return (
    <Stack spacing={4}>
      <PageEntityRender {...pageEntityMetadata}></PageEntityRender>
      <ActionJobTable {...actionJobTableParams} actionId={actionId}></ActionJobTable>
      <ProcessTracking isLoading={processTracking}></ProcessTracking>
      <SnackbarAlert {...snackbarAlertMetadata}></SnackbarAlert>
      <ConfirmationDialog {...confirmationDeleteDialogMeta}></ConfirmationDialog>
    </Stack >
  );
}