import Box from '@mui/material/Box';
import SpeedDial from '@mui/material/SpeedDial';
import SpeedDialAction from '@mui/material/SpeedDialAction';
import SpeedDialIcon from '@mui/material/SpeedDialIcon';
import * as React from 'react';

export default function FloatingSpeedDialButtons(props: any) {

  const actions = props.actions;
  return (
    <Box sx={{ height: 320, transform: 'translateZ(0px)', flexGrow: 1, position: 'absolute', bottom: 16, right: 16  }}>
      <SpeedDial
        ariaLabel="SpeedDial basic example"
        sx={{ position: 'absolute', bottom: 16, right: 16 }}
        icon={<SpeedDialIcon />}
      >
        {actions.map((action: any) => (
          <SpeedDialAction
            key={action.name}
            icon={action.icon}
            FabProps={action.properties}
            tooltipTitle={action.name}
            onClick={action.onClick}
          />
        ))}
      </SpeedDial>
    </Box>
  );
}