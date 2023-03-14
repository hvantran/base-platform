import * as React from 'react';
import { Link } from "react-router-dom";
import { ViewUpdate } from "@codemirror/view";
import { LanguageSupport } from '@codemirror/language';


export default function withLink(to: any, children: any) {
    return <Link to={to}>{children}</Link>
};

export enum PropType {
    InputText,
    Textarea,
    Selection,
    CodeEditor
}

export interface TextFieldMetadata {
    onChangeEvent: React.ChangeEventHandler<HTMLTextAreaElement | HTMLInputElement> | undefined
}

export interface CodeEditorMetadata {
    codeLanguges: Array<LanguageSupport>
    onChangeEvent:((value: string, viewUpdate: ViewUpdate) => void) | undefined
}

export interface SelectionMetadata {
    selections: Array<String>
    onChangeEvent: CallableFunction
}

export interface PropertyMetadata {
    propName: string
    propType: PropType
    propLabel?: string
    propDescription?: string

    codeEditorMeta?: CodeEditorMetadata
    selectionMeta?: SelectionMetadata
    textFieldMeta?: TextFieldMetadata

}

export interface EntityMetadata {
    properties: Array<PropertyMetadata>
}

export interface Dictionary<T> {
    [Key: string]: T;
}

export interface SpeedDialActionMetadata {
    actionName: string
    actionLabel: string
    actionIcon: any
    properties: Dictionary<string>
    onClick: CallableFunction
}


export interface PageEntityMetadata {
    floatingActions: Array<SpeedDialActionMetadata>
    templateStepMetadata: StepMetadata
}

export interface StepMetadata extends EntityMetadata {
    name: string
    label: string
    isOptional?: boolean
    description?: string
}
