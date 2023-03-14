import { LanguageSupport } from '@codemirror/language';
import { ViewUpdate } from "@codemirror/view";
import CodeMirror from '@uiw/react-codemirror';
import React from 'react';

function CodeEditor(props: CodeEditorProps) {
    return (
        <CodeMirror
            value={props.value}
            height={props.height ? props.height : "500px"}
            extensions={props.language}
            onChange={props.onChange}
        />
    );
}
export interface CodeEditorProps {
    language: Array<LanguageSupport>,
    value: string,
    height?: string,
    onChange ? (value: string, viewUpdate: ViewUpdate): void;
}
export default CodeEditor;