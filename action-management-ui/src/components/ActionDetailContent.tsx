
import { Stack } from '@mui/material';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import React from 'react';
import Breadcrumbs from './Breadcrumbs';

import { useParams } from 'react-router-dom';
import ActionContentControlButtons from './ActionContentControlButtons';


export default function ActionDetailContent() {

  const targetAction = useParams();
  const actionId = targetAction.actionId

  const breadcrumbs = [
    <Link underline="hover" key="1" color="inherit" href="/actions">Actions</Link>,
    <Typography key="3" color="text.primary">{actionId}</Typography>,
  ];


  return (
    <Stack spacing={4}>
      <Breadcrumbs breadcrumbs={breadcrumbs} />
      <ActionContentControlButtons />
    </Stack >
  );
}