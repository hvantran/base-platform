import { Stack } from '@mui/material';
import React from 'react';
import { Route, Routes } from "react-router-dom";
import ActionSummary from './components/actions/ActionSummary';
import ActionCreation from './components/actions/ActionCreation';
import ActionDetail from './components/actions/ActionDetail';
import ErrorPage from './components/common/ErrorPage';
import HomeContent from './components/HomeContent';
import PrimarySearchAppBar from './ResponsiveAppBar';
import JobSummary from './components/jobs/JobSummary';
import JobDetails from './components/jobs/JobDetails';


function App() {
  return (
    <Stack spacing={4}>
      <PrimarySearchAppBar />
      <Routes>
        <Route path='/' element={<HomeContent />} errorElement={<ErrorPage />}></Route>
        <Route path='/actions' element={<ActionSummary />}></Route>
        <Route path='/actions/:actionId' element={<ActionDetail />}></Route>
        <Route path='/actions/new' element={<ActionCreation />}></Route>
        <Route path='jobs' element={<JobSummary />}></Route>
        <Route path='/actions/:actionId/jobs/:jobId' element={<JobDetails />}></Route>
      </Routes>
    </Stack>
  );
}
export default App;