import Box from '@mui/material/Box';
import SpeedDial from '@mui/material/SpeedDial';
import SpeedDialAction from '@mui/material/SpeedDialAction';
import SpeedDialIcon from '@mui/material/SpeedDialIcon';
import * as React from 'react';
import { SpeedDialActionMetadata } from '../GenericConstants';

export default function FloatingSpeedDialButtons(props: any) {

  const actions: Array<SpeedDialActionMetadata> = props.actions;
  return (
    <Box sx={{ height: 320, transform: 'translateZ(0px)', flexGrow: 1, position: 'absolute', bottom: 16, right: 16  }}>
      <SpeedDial
        ariaLabel="SpeedDial basic example"
        sx={{ position: 'absolute', bottom: 16, right: 16 }}
        icon={<SpeedDialIcon />}
      >
        {actions.map((action: SpeedDialActionMetadata) => (
          <SpeedDialAction
            key={action.actionName}
            icon={action.actionIcon}
            FabProps={action.properties}
            tooltipTitle={action.actionLabel}
            onClick={action.onClick}
          />
        ))}
      </SpeedDial>
    </Box>
  );
}