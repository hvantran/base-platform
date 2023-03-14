import CreateIcon from '@mui/icons-material/CreateOutlined';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import FavoriteIcon from '@mui/icons-material/GradeOutlined';
import InfoTwoToneIcon from '@mui/icons-material/InfoTwoTone';
import { green, red, yellow } from '@mui/material/colors';
import * as React from 'react';
import { Link } from 'react-router-dom';
import withLink from './AppConstants';
import FloatingSpeedDialButtons from './FloatingActions';


const actions = [
  { icon: withLink('/actions/new', <CreateIcon/>), name: 'Create' , properties: {
    sx: {
      bgcolor: green[500],
      '&:hover': {
        bgcolor: green[800],
      }
    }
  }},
  { icon: <DeleteIcon/>, name: 'Delete' , properties: {
    sx: {
      bgcolor: red[500],
      '&:hover': {
        bgcolor: red[800],
      }
    }
  }},
  { icon: <FavoriteIcon />, name: 'Favorite' , properties: {
    sx: {
      bgcolor: yellow[500],
      '&:hover': {
        bgcolor: yellow[800],
      }
    }
  }},
  { icon: <InfoTwoToneIcon/>, name: 'Go to details' }
];

export default function ActionContentControlButtons() {

  return (
    <FloatingSpeedDialButtons actions={actions} />
  );
}