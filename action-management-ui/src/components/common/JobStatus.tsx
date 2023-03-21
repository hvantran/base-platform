import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import PendingIcon from '@mui/icons-material/Pending';
import { CircularProgress } from '@mui/material';
import { red, yellow } from "@mui/material/colors";
import React from "react";


export default function JobStatus(props: any) {

    let status = props.status as string;

    if (!status) {
        throw new Error("Status value cannot be undefined/empty/null");
    }
    switch (status) {
        case "SUCCESS":
            return (<CheckCircleIcon fontSize="large" color="success" />)
        case "FAILURE":
            return (<ErrorIcon fontSize="large" sx={{ color: red[900] }} />)
        case "PROCESSING":
            return (<CircularProgress size='35px'/>)
        case "PENDING":
        default:
            return (<PendingIcon fontSize="large" sx={{ color: yellow[900] }} />)
    }
}