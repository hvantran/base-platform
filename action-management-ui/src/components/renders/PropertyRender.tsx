import { FormControl, Grid, Input, MenuItem, Select, Switch, TextField } from '@mui/material';
import * as React from 'react';
import CodeEditor from '../common/CodeEditor';
import { PropertyMetadata, PropType } from '../GenericConstants';

export default function PropertyRender(props: any) {
    let property: PropertyMetadata = props.property
    switch (property.propType) {

        case PropType.CodeEditor:
            if (!property.codeEditorMeta) {
                throw new Error(`codeEditorMeta is required for ${property.propName} code editor property`);
            }
            let codeEditorMeta = property.codeEditorMeta
            return (
                <Grid container spacing={2} sx={{ py: 1 }}>
                    <Grid item xs={1}>
                        <label>{property.propLabel} {property.isRequired ? (<span>*</span>) : (<span />)}</label>
                    </Grid>
                    <Grid item xs={11}>
                        <CodeEditor
                            key={property.propName}
                            isRequired={property.isRequired}
                            propName={property.propName}
                            value={property.propValue}
                            height={codeEditorMeta.height}
                            onChange={codeEditorMeta.onChangeEvent}
                            language={codeEditorMeta.codeLanguges} />
                    </Grid>
                </Grid>
            )

        case PropType.Selection:
            if (!property.selectionMeta) {
                throw new Error(`selectionMeta is required for ${property.propName} selection property`);
            }
            let selectionMeta = property.selectionMeta
            return (
                <Grid container spacing={2} sx={{ py: 1 }}>
                    <Grid item xs={1}>
                        <label>{property.propLabel} {property.isRequired ? (<span>*</span>) : (<span />)}</label>
                    </Grid>
                    <Grid item xs={11}>
                        <FormControl sx={{ m: 1, width: '100%' }} size="small">
                            <Select
                                labelId={"demo-select-small".concat(property.propName)}
                                id={"demo-select-small".concat(property.propName)}
                                value={property.propValue}
                                name={property.propName}
                                label={property.propLabel}
                                onChange={selectionMeta.onChangeEvent}
                            >  {
                                    selectionMeta.selections.map(selection => {
                                        return (<MenuItem key={property.propName + selection} value={selection}>{selection}</MenuItem>)
                                    })
                                }
                            </Select>
                        </FormControl>
                    </Grid>
                </Grid>
            )

        case PropType.Textarea:
            if (!property.textareaFieldMeta) {
                throw new Error(`textareaFieldMeta is required for ${property.propName} text property`);
            }
            let textareaFieldMeta = property.textareaFieldMeta
            return (
                <Grid container spacing={2} sx={{ py: 1 }}>
                    <Grid item xs={1}>
                        <label>{property.propLabel} {property.isRequired ? (<span>*</span>) : (<span />)}</label>
                    </Grid>
                    <Grid item xs={11}>
                        <TextField
                            key={property.propName}
                            required={property.isRequired}
                            name={property.propName}
                            value={property.propValue}
                            size="small"
                            sx={{ width: '100%' }}
                            multiline
                            onChange={textareaFieldMeta.onChangeEvent}
                            variant="standard" />
                    </Grid>
                </Grid>
            )
        case PropType.Switcher:
            if (!property.switcherFieldMeta) {
                throw new Error(`switcherFieldMeta is required for ${property.propName} boolean property`);
            }
            let switcherFieldMeta = property.switcherFieldMeta
            return (
                <Grid container spacing={2} sx={{ py: 1 }}>
                    <Grid item xs={1}>
                        <label>{property.propLabel} {property.isRequired ? (<span>*</span>) : (<span />)}</label>
                    </Grid>
                    <Grid item xs={11}>
                        <Switch
                            key={property.propName}
                            name={property.propName}
                            checked={property.propValue}
                            onChange={switcherFieldMeta.onChangeEvent} />
                    </Grid>
                </Grid>
            )
        case PropType.InputText:
        default:
            if (!property.textFieldMeta) {
                throw new Error(`textFieldMeta is required for ${property.propName} text property`);
            }
            let textFieldMeta = property.textFieldMeta
            return (
                <Grid container spacing={2} sx={{ py: 1 }}>
                    <Grid item xs={1}>
                        <label>{property.propLabel} {property.isRequired ? (<span>*</span>) : (<span />)}</label>
                    </Grid>
                    <Grid item xs={11}>
                        <Input required={property.isRequired}
                            size="small"
                            sx={{ width: '100%' }}
                            name={property.propName}
                            value={property.propValue}
                            onChange={textFieldMeta.onChangeEvent}
                            placeholder={textFieldMeta.placeholder} />
                    </Grid>
                </Grid>
            )
    }
}