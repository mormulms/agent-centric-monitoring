import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { SchemaFormModule, WidgetRegistry } from 'ngx-schema-form';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgxGraphModule } from '@swimlane/ngx-graph';
import { NgxChartsModule } from '@swimlane/ngx-charts';

import { TemplateGraphComponent } from '@mona/graph/template-graph.component';
import { AggregatorNodeBodyComponent } from '@mona/graph/aggregator/aggregator-node-body.component';
import { BsCheckboxWidgetComponent } from '@mona/widgets/bs-checkbox-widget.component';
import { NodeBodyComponent } from '@mona/graph/node-body.component';
import { FileNodeBodyComponent } from '@mona/graph/output/file-node-body.component';
import { ComparisonNodeBodyComponent } from '@mona/graph/processor/comparison-node-body.component';
import { CustomWidgetRegistry } from '@mona/widgets/CustomWidgetRegistry';
import { TemplateRoutingModule } from '@mona/template/template-routing.module';
import { BsButtonWidgetComponent } from '@mona/widgets/bs-button-widget.component';
import { TemplateEditorComponent } from '@mona/template/template-editor.component';
import { BsArrayWidgetComponent } from '@mona/widgets/bs-array-widget.component';
import { CpuNodeBodyComponent } from '@mona/graph/input/cpu-node-body.component';
import { OperandComponent } from '@mona/graph/operand.component';
import { StatsNodeBodyComponent } from '@mona/graph/aggregator/stats-node-body.component';
import { DbNodeBodyComponent } from '@mona/graph/output/db-node-body.component';
import { NodeComponent } from '@mona/graph/node.component';
import { LogicNodeBodyComponent } from '@mona/graph/aggregator/logic-node-body.component';
import { InputNodeBodyComponent } from '@mona/graph/input/input-node-body.component';
import { CalcNodeBodyComponent } from '@mona/graph/processor/calc-node-body.component';
import { RangeNodeBodyComponent } from '@mona/graph/processor/range-node-body.component';
import { BsFloatWidgetComponent } from '@mona/widgets/bs-float-widget.component';
import { ProcstatNodeBodyComponent } from '@mona/graph/input/procstat-node-body.component';
import { BsStringWidgetComponent } from '@mona/widgets/bs-string-widget.component';

@NgModule({
  imports: [
    CommonModule,
    SchemaFormModule.forRoot(),
    ReactiveFormsModule,
    TemplateRoutingModule,
    FontAwesomeModule,
    NgxChartsModule,
    NgxGraphModule,
  ],
  declarations: [
    TemplateEditorComponent,
    BsButtonWidgetComponent,
    BsArrayWidgetComponent,
    BsCheckboxWidgetComponent,
    BsFloatWidgetComponent,
    BsStringWidgetComponent,
    TemplateGraphComponent,
    NodeComponent,
    NodeBodyComponent,
    InputNodeBodyComponent,
    ComparisonNodeBodyComponent,
    RangeNodeBodyComponent,
    CalcNodeBodyComponent,
    AggregatorNodeBodyComponent,
    StatsNodeBodyComponent,
    LogicNodeBodyComponent,
    DbNodeBodyComponent,
    FileNodeBodyComponent,
    CpuNodeBodyComponent,
    ProcstatNodeBodyComponent,
    OperandComponent,
  ],
  entryComponents: [
    BsButtonWidgetComponent,
    BsArrayWidgetComponent,
    BsCheckboxWidgetComponent,
    BsFloatWidgetComponent,
    BsStringWidgetComponent,
  ],
  providers: [
    { provide: WidgetRegistry, useClass: CustomWidgetRegistry }
  ],
})
export class TemplateModule { }
