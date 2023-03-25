import * as React from 'react';
import { Link } from "react-router-dom";
import { ViewUpdate } from "@codemirror/view";
import { LanguageSupport } from '@codemirror/language';
import { SelectChangeEvent } from '@mui/material';

export function WithLink(to: any, children: any) {
    return <Link to={to}>{children}</Link>
};

export class RestClient {
    setCircleProcessOpen:(value: boolean) => void
    setMessageInfo:(message: SnackbarMessage) => void
    setOpenError:(value: boolean) => void
    setOpenSuccess:(value: boolean) => void

    constructor(setCircleProcessOpen:(value: boolean) => void,
    setMessageInfo:(message: SnackbarMessage) => void,
    setOpenError:(value: boolean) => void,
    setOpenSuccess:(value: boolean) => void
    )  {
        this.setCircleProcessOpen = setCircleProcessOpen;
        this.setMessageInfo = setMessageInfo;
        this.setOpenError = setOpenError;
        this.setOpenSuccess = setOpenSuccess;
    }

    async sendRequest(requestOptions: any, targetURL: string, successCallback: (response: Response) => Promise<SnackbarMessage>, errorCallback: (response: Response) => Promise<SnackbarMessage>) {

          try {
            this.setCircleProcessOpen(true);
            let response = await fetch(targetURL, requestOptions);
            if (!response.ok) {
              let errorSnackbarMessage = errorCallback(response);
              this.setMessageInfo(await errorSnackbarMessage);
              this.setOpenError(true);
              return;
            }
      
            let successSnackbarMessage = successCallback(response);
            this.setMessageInfo(await successSnackbarMessage);
            this.setOpenSuccess(true);
          } catch (error: any) {
            let messageInfo = { 'message': "An interal error occurred during your request!", key: new Date().getTime() } as SnackbarMessage;
            this.setMessageInfo(messageInfo);
            this.setOpenError(true);
          } finally {
            this.setCircleProcessOpen(false);
          }
    }
}

export interface SnackbarAlertMetadata {
    openError: boolean
    openSuccess: boolean
    setOpenError: (previous: any) => void
    setOpenSuccess: (previous: any) => void
    messageInfo: SnackbarMessage | undefined
}

export interface SnackbarMessage {
    message: string;
    key: number;
}

export enum PropType {
    InputText,
    Textarea,
    Selection,
    CodeEditor,
    Switcher
}

export interface TextFieldMetadata {
    placeholder?: string
    onChangeEvent: React.ChangeEventHandler<HTMLTextAreaElement | HTMLInputElement> | undefined
}

export interface CodeEditorMetadata {
    height?: string
    codeLanguges: Array<LanguageSupport>
    onChangeEvent: (propertyName: string) => (value: string, viewUpdate: ViewUpdate) => void
}

export interface SwitcherFieldMeta {
    onChangeEvent: (event: React.ChangeEvent<HTMLInputElement>, checked: boolean) => void
}


export interface SelectionMetadata {
    selections: Array<string>
    onChangeEvent: (event: SelectChangeEvent, child: React.ReactNode) => void;
}

export interface PropertyMetadata {
    propName: string
    propValue: any
    propDefaultValue?: any
    propType: PropType
    propLabel?: string
    isRequired?: boolean
    propDescription?: string
    layoutProperties?: any
    labelElementProperties?: any
    valueElementProperties?: any

    codeEditorMeta?: CodeEditorMetadata
    selectionMeta?: SelectionMetadata
    textFieldMeta?: TextFieldMetadata
    textareaFieldMeta?: TextFieldMetadata
    switcherFieldMeta?: SwitcherFieldMeta

}

export interface EntityMetadata {
    properties: Array<PropertyMetadata>
}

export interface ActionMetadata {
    actionName: string
    actionLabel: string
    actionIcon: any
    properties?: any
}

export interface ColumnActionMetadata extends ActionMetadata {
    onClick: (row: any) => (event: any) => void
}

export interface SpeedDialActionMetadata extends ActionMetadata {
    onClick?: React.MouseEventHandler<HTMLDivElement>
}

export interface ColumnMetadata {
    label: string
    id: string
    isHidden?: boolean
    minWidth?: number
    isKeyColumn?: boolean
    align?: "center" | "right" | "left" | "inherit" | "justify" | undefined
    format?: (value: any) => string | React.ReactNode
    actions?: Array<ColumnActionMetadata>
}

export interface PagingOptionMetadata {

    rowsPerPageOptions: Array<number | { value: number; label: string }>
    component: string | "div"
    pageSize: number
    pageIndex: number
    onPageChange: (pageIndex: number, pageSize: number) => void
}

export interface PagingResult {
    totalElements: number
    content: Array<any>
    pageable?: any
}

export interface TableMetadata {
    columns: Array<ColumnMetadata>
    pagingOptions: PagingOptionMetadata
    pagingResult: PagingResult
}


export interface PageEntityMetadata {
    pageName: string
    floatingActions?: Array<SpeedDialActionMetadata>
    stepMetadatas?: Array<StepMetadata>
    tableMetadata?: TableMetadata
    properties?: Array<PropertyMetadata>
}

export interface StepMetadata extends EntityMetadata {
    name: string
    label: string
    isOptional?: boolean
    description?: string
    onFinishStepClick?: (currentStepMetadata: Array<StepMetadata>) => void
}