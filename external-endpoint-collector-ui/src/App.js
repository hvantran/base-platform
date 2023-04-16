import { Stack, ThemeProvider } from '@mui/material';
import React from 'react';
import { Route, Routes } from "react-router-dom";
import PrimarySearchAppBar from './ResponsiveAppBar';
import HomeContent from './components/HomeContent';
import ExtEndpointSummary from './components/ext-endpoints/ExtEndpointSummary';
import ExtEndpointDetails from './components/ext-endpoints/ExtEndpointDetails';
import ExtEndpointResponseDetails from './components/ext-endpoints/ExtEndpointResponseDetails';
import ExtEndpointCreation from './components/ext-endpoints/ExtEndpointCreation';
import ErrorPage from './components/common/ErrorPage';
import { DEFAULT_THEME } from './components/GenericConstants';


function App() {
  return (
    <ThemeProvider theme={DEFAULT_THEME}>
      <Stack spacing={4}>
        <PrimarySearchAppBar />
        <Routes>
          <Route path='/' element={<HomeContent />} errorElement={<ErrorPage />}></Route>
          <Route path='/endpoints' element={<ExtEndpointSummary />}></Route>
          <Route path='/endpoints/new' element={<ExtEndpointCreation />}></Route>
          <Route path='/endpoints/:application' element={<ExtEndpointDetails />}></Route>
          <Route path='/endpoints/:application/responses/:response' element={<ExtEndpointResponseDetails />}></Route>
        </Routes>
      </Stack>
    </ThemeProvider>
  );
}
export default App;