
import { Stack } from '@mui/material';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import React from 'react';
import Breadcrumbs from '../common/Breadcrumbs';

import { useParams } from 'react-router-dom';
import { ActionDetails, ACTION_MANAGER_API_URL } from '../AppConstants';
import ProcessTracking from '../common/ProcessTracking';
import { PageEntityMetadata, PropType } from '../GenericConstants';
import PageEntityRender from '../renders/PageEntityRender';
import ActionJobTable from './ActionJobTable';


export default function ActionDetail() {

  const targetAction = useParams();
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
  const [actionDetailData, setActionDetailData] = React.useState(initialActionDetailData);

  const breadcrumbs = [
    <Link underline="hover" key="1" color="inherit" href="/actions">Actions</Link>,
    <Typography key="3" color="text.primary">{actionId}</Typography>,
  ];

  const fetchActionDetailAsync = async () => {
    const requestOptions = {
      method: "GET",
      headers: {
        "Accept": "application/json"
      }
    }

    try {
      setCircleProcessOpen(true);
      let response = await fetch(`${ACTION_MANAGER_API_URL}/${encodeURIComponent(actionId)}`, requestOptions);
      let actionDetailResult = await response.json() as ActionDetails;
      setActionDetailData(actionDetailResult);
    } finally {
      setCircleProcessOpen(false);
    }
  }

  React.useEffect(() => {
    fetchActionDetailAsync();
  }, []);

  let pageEntityMetadata: PageEntityMetadata = {
    pageName: 'action-details',
    properties: [
      {
        propName: 'actionName',
        propLabel: 'Name',
        propValue: actionDetailData.name,
        isRequired: true,
        propDescription: 'This is name of action',
        propType: PropType.InputText
      }
    ]
  }



  return (
    <Stack spacing={4}>
      <Breadcrumbs breadcrumbs={breadcrumbs} />
      <PageEntityRender {...pageEntityMetadata}></PageEntityRender>
      <ActionJobTable setCircleProcessOpen={setCircleProcessOpen} actionId={actionId}></ActionJobTable>
      <ProcessTracking isLoading={processTracking}></ProcessTracking>
    </Stack >
  );
}