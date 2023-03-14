
import React from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { javascript } from '@codemirror/lang-javascript';

export default function JobContent() {

    const onChange = React.useCallback((value: any, viewUpdate: any) => {
      console.log('value:', value);
    }, []);

    return 
    <CodeMirror
      value="console.log('hello world!');"
      height="200px"
      extensions={[javascript({ jsx: true })]}
      onChange={onChange}
    />
}