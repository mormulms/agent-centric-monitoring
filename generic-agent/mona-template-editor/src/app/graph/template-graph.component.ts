import { Component, EventEmitter, HostListener, Input, OnDestroy, OnInit, Output } from '@angular/core';

import * as shape from 'd3-shape';
import { faCogs, faSignInAlt, faSignOutAlt } from '@fortawesome/free-solid-svg-icons';
import { Observable, Subject, Subscription } from 'rxjs';

import { Node } from '@mona/template/template.service';

export interface Graph {
  nodes: Node[];
  links: Link[];
}

export interface Link {
  source: string;
  target: string;
  label: string;
  id?: string;
}

interface CustomColor {
  name: string;
  value: string;
}

interface Shadow {
  name: string;
  stdDeviation: number;
  color: string;
  opacity: number;
}

@Component({
  selector: 'app-template-graph',
  templateUrl: './template-graph.component.html',
  styleUrls: ['./template-graph.component.scss']
})
export class TemplateGraphComponent implements OnInit, OnDestroy {

  @Input() graph: Observable<Graph>;

  @Output() newEdge: EventEmitter<Link> = new EventEmitter();
  @Output() nodeDelete: EventEmitter<Node> = new EventEmitter();
  @Output() deleteEdge: EventEmitter<Link> = new EventEmitter();
  @Output() selectNode: EventEmitter<Node> = new EventEmitter();

  private graphSub: Subscription;

  currentGraph: Graph = { nodes: [], links: [] };

  width = 1600;
  height = 900;
  view: number[] = [this.width, this.height];

  // observables
  update$: Subject<any> = new Subject();
  center$: Subject<any> = new Subject();
  zoomToFit$: Subject<any> = new Subject();

  enableZoom = true;
  autoZoom = false;
  autoCenter = false;
  panOnZoom = true;

  curve: any = shape.curveBundle;

  orientation = 'LR';

  colorScheme = 'picnic';
  customColors: CustomColor[] = [
    { name: 'input', value: '#0e9aad' },
    { name: 'processor', value: '#869b3d' },
    { name: 'aggregator', value: '#d88b1f' },
    { name: 'output', value: '#d5573b' },
  ];

  shadows: Shadow[] = [
    { name: 'drop', stdDeviation: 5, color: '#000000', opacity: 0.5 },
    { name: 'input', stdDeviation: 5, color: '#0e9aad', opacity: 1 },
    { name: 'processor', stdDeviation: 5, color: '#869b3d', opacity: 1 },
    { name: 'aggregator', stdDeviation: 5, color: '#d88b1f', opacity: 1 },
    { name: 'output', stdDeviation: 5, color:  '#d5573b', opacity: 1 },
  ];

  // icons
  faSignInAlt = faSignInAlt;
  faSignOutAlt = faSignOutAlt;
  faCogs = faCogs;

  getNodeTypeFn: (n: any) => string = this.getNodeType;

  private readonly emptyLink: Link = { id: '', source: '', target: '', label: '' };
  selectedLink: Link = this.emptyLink;

  constructor() { }

  ngOnInit() {
    this.graphSub = this.graph.subscribe((newGraph) => this.updateGraph(newGraph));
  }

  ngOnDestroy(): void {
    if (this.graphSub) {
      this.graphSub.unsubscribe();
    }
  }

  private updateGraph(newGraph: Graph): void {
    this.currentGraph = newGraph;
    this.update$.next(true);
  }

  private getNodeType(node: Node): string {
    return node.nodeType;
  }

  @HostListener('document:keyup.escape', ['$event'])
  onEscapePressed(event: KeyboardEvent): void {
    const updatedNodes = this.currentGraph.nodes.map(n => ({...n, selected: false}));
    this.selectedLink = this.emptyLink;

    this.updateGraph({...this.currentGraph, nodes: updatedNodes});
  }

  @HostListener('document:keyup.delete')
  onDeletePressed(event: KeyboardEvent): void {
    if (this.selectedLink && this.selectedLink.id && this.selectedLink.id.length > 0) {
      this.deleteEdge.emit(this.selectedLink);
      return;
    }

    const nodeToDelete = this.currentGraph.nodes.find(n => n.selected);

    if (nodeToDelete) {
      this.nodeDelete.emit(nodeToDelete);
    }
  }

  public select(selectedNode: Node) {
    console.log({selectedNode});
    this.selectedLink = this.emptyLink;

    const nodes = this.currentGraph.nodes;
    const selectedIndex = nodes.findIndex(n => n.id === selectedNode.id);
    if (selectedIndex === -1) {
      throw new Error(`Selected node not found!`);
    }
    const targetNode = nodes[selectedIndex];

    const prevSelectedIndex = nodes.findIndex(n => n.id !== selectedNode.id && n.selected);
    if (prevSelectedIndex !== -1) {
      const sourceNode = nodes[prevSelectedIndex];
      this.newEdge.emit({source: sourceNode.id, target: targetNode.id, label: sourceNode.id});
      return;
    }

    // immutable splice
    const updatedNodes = [
      ...nodes.slice(0, selectedIndex),
      {...targetNode, selected: !targetNode.selected }, // toggle selected flag
      ...nodes.slice(selectedIndex + 1)
    ];

    this.updateGraph({...this.currentGraph, nodes: updatedNodes});

    if (updatedNodes[selectedIndex].selected) {
      this.selectNode.emit(targetNode);
    } else {
      this.selectNode.emit(null);
    }
  }

  public selectEdge(selectedLink: Link) {
    if (!selectedLink.id || selectedLink.id.length < 1) {
      return;
    }

    // deselect all nodes
    const updatedNodes = this.currentGraph.nodes.map(n => ({...n, selected: false}));
    this.updateGraph({...this.currentGraph, nodes: updatedNodes});

    this.selectedLink = (this.selectedLink.id === selectedLink.id) ? this.emptyLink :  {...selectedLink};
  }

  shadowTrackByFn(index: number, item: Shadow) {
    return item.name;
  }

}
