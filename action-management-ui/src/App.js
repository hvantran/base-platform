import { Stack } from '@mui/material';
import React from 'react';
import { Route, Routes } from "react-router-dom";
import ActionContent from './components/ActionContent';
import ActionCreationContent from './components/ActionCreationContent';
import ActionDetailContent from './components/ActionDetailContent';
import ErrorPage from './components/ErrorPage';
import HomeContent from './components/HomeContent';
import JobContent from './components/JobContent';
import PrimarySearchAppBar from './ResponsiveAppBar';


function App() {
  return (
    <Stack spacing={4}>
      <PrimarySearchAppBar />
      <Routes>
        <Route path='/' element={<HomeContent />} errorElement={<ErrorPage />}></Route>
        <Route path='/actions' element={<ActionContent />}></Route>
        <Route path='/actions/:actionId' element={<ActionDetailContent />}></Route>
        <Route path='/actions/new' element={<ActionCreationContent />}></Route>
        <Route path='jobs' element={<JobContent />}></Route>
      </Routes>
    </Stack>
  );
}
export default App;