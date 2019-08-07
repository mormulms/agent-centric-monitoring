import { AfterViewChecked, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { faFileDownload, faFileUpload, faSave } from '@fortawesome/free-solid-svg-icons';
import { forkJoin, Observable, ReplaySubject, Subscription } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';

import { AgentTemplateSchema, SchemaHttpService } from '@mona/core/http/schema-http.service';
import { Graph, Link } from '@mona/graph/template-graph.component';
import { AgentTemplate, NextRef, NextWrapper, Node, TemplateNode, TemplateService } from '@mona/template/template.service';
import { AuthTokenProviderService } from '@mona/core/auth-token-provider.service';
import { ConfigProviderService } from '@mona/template/config-provider.service';
import { CcTokenProviderService } from '@mona/core/cc-token-provider.service';
import { ConfigFileHttpService } from '@mona/core/http/config-file-http.service';
import { AuthHttpService } from '@mona/core/http/auth-http.service';
import { TemplateHttpService } from '@mona/core/http/template-http.service';

@Component({
  selector: 'app-template-editor',
  templateUrl: './template-editor.component.html',
  styleUrls: ['./template-editor.component.scss']
})
export class TemplateEditorComponent implements OnInit, AfterViewChecked, OnDestroy {

  @ViewChild('form') templateForm: ElementRef;

  schema$: Observable<AgentTemplateSchema>;

  savedTemplate: AgentTemplate;

  template: AgentTemplate = { id: '', name: '', inputs: [], aggregators: [], processors: [], outputs: [] };

  private _graph: ReplaySubject<Graph> = new ReplaySubject();
  graph = this._graph.asObservable();

  private readonly nextTypeMap: { [key: string]: string } = {
    // Inputs
    IGenericInput: 'INextInput',
    ICPUInput: 'INextInput',
    IMemoryInput: 'INextInput',

    // Processors
    ICalculationProcessor: 'INextProcessor',
    IComparisonProcessor: 'INextProcessor',
    IRangeProcessor: 'INextProcessor',

    // Aggregators
    IStatsAggregator: 'INextAggregator',
    ILogicAggregator: 'INextAggregator',

    // Output
    IGenericOutput: 'INextOutput',
    IDatabaseOutput: 'INextOutput',
    IFileOutput: 'INextOutput',
  };

  faFileDownload = faFileDownload;
  faFileUpload = faFileUpload;
  faSave = faSave;

  fragment: string;

  private templateSub: Subscription;
  private fragmentSub: Subscription;

  constructor(private templateService: TemplateService,
              private templateHttpService: TemplateHttpService,
              private schemaHttpService: SchemaHttpService,
              private configProvider: ConfigProviderService,
              private ccTokenProvider: CcTokenProviderService,
              private authHttpService: AuthHttpService,
              private authTokenProvider: AuthTokenProviderService,
              private configFileHttpService: ConfigFileHttpService,
              private route: ActivatedRoute,
              private router: Router) { }

  ngOnInit(): void {
    this.schema$ = this.schemaHttpService.getSchema();

    this.templateService.template$.subscribe(
      (newTemplate) => {
        this.savedTemplate = newTemplate;
        this.template = newTemplate;
        this.updateGraph(newTemplate);
      }
    );

    this.fragmentSub = this.route.fragment.subscribe(fragment => this.fragment = fragment);
  }

  ngAfterViewChecked(): void {
    try {
      if (this.fragment && this.fragment.length > 1) {
        const anchorElementId = `form-${this.fragment}`;

        const el = document.getElementById(anchorElementId);
        if (!el) {
          return;
        }

        const rect = el.getBoundingClientRect();
        const nativeNav = this.templateForm.nativeElement;

        if (rect && nativeNav) {
          nativeNav.scrollTop += rect.top - 190;
        }
      }
    } catch (err) {
      console.error(err);
    } finally {
      this.fragment = null;
    }
  }

  ngOnDestroy(): void {
    if (this.templateSub) {
      this.templateSub.unsubscribe();
    }

    if (this.fragmentSub) {
      this.fragmentSub.unsubscribe();
    }
  }

  saveTemplate(newTemplate: AgentTemplate) {
    this.templateService.saveTemplate(newTemplate);
    this.savedTemplate = newTemplate;
    this.template = newTemplate;
    this.updateGraph(newTemplate);
  }

  private updateGraph(template: AgentTemplate) {
    const templateNodes: Node[] = [
      ...template.inputs.map(n => ({...n, nodeType: 'input'})),
      ...template.processors.map(p => ({...p, nodeType: 'processor'})),
      ...template.aggregators.map(a => ({...a, nodeType: 'aggregator'})),
      ...template.outputs.map(o => ({...o, nodeType: 'output'})),
    ]
    .filter(n => n._type && n._type.length > 0)
    .map(n => (<Node>{...n[n._type], nodeType: n.nodeType}));

    const nodes = templateNodes
    .map((node, i) => {
      const id = node.id || 'node' + i;
      const label = node.name || id;
      return {...node, id, label};
    });

    const links = templateNodes.reduce((acc, currentNode, index, tnodes) => {
      if (!currentNode || !currentNode.next) {
        return acc;
      }

      const furtherLinks: Link[] = currentNode.next
      .filter(next => next._type && next._type.length > 0)
      .map(nextWrapper => <NextRef>nextWrapper[nextWrapper._type])
      .filter(next => {
        // TODO also prevent bigger cycles
        if (!next || !next.value || next.value.id === currentNode.id) {
          return false;
        }

        // check if target exists
        return tnodes.some(n => n.id === next.value.id && n.nodeType === next.value.type);
      })
      .map(next => ({source: currentNode.id, target: next.value.id, label: currentNode.id}));

      return [...acc, ...furtherLinks];
    }, <Link[]>[]);

    console.log(`New graph with ${nodes.length} nodes and ${links.length} edges.`);

    this._graph.next({ nodes, links });
  }

  addEdge(newLink: Link): void {
    console.log({newLink});
    const targetNode = this.getNode(newLink.target);

    const targetType = this.nextTypeMap[targetNode._type];
    const targetNodeType = <string>targetNode.nodeType;
    if (targetNodeType === 'input') {
      return;
    }
    const newNext: NextWrapper = { _type: targetType, [targetType]: { $type: 'ref', value: { type: targetNodeType, id: newLink.target }}};

    // TODO prevent outbound links from outputs, backward links
    let updatedTemplate;
    try {
      updatedTemplate = this.updateSource(newLink.source, newNext);
      this.updateTemplate(updatedTemplate);
    } catch (error) {
      console.warn(error);
    }
  }

  private updateTemplate(updatedTemplate: AgentTemplate) {
    this.template = updatedTemplate;
    this.updateGraph(this.template);
  }

  public deleteNode(nodeToDelete: Node) {
    const idToDelete = nodeToDelete.id;
    const type = this.getNodeTypeById(idToDelete);
    const index = this.findIndex(this.template[type], idToDelete);
    const updatedNodes = [...this.template[type].slice(0, index), ...this.template[type].slice(index + 1)];
    this.deleteOrphanedLinks(idToDelete);
    this.updateTemplate({...this.template, [type]: updatedNodes});
  }

  public deleteEdge(linkToDelete: Link) {
    const templateNode = this.getNode(linkToDelete.source);
    const sourceNode = <Node>templateNode[templateNode._type];

    const updatedRefs = sourceNode.next.filter(wrapper => (<NextRef>wrapper[wrapper._type]).value.id !== linkToDelete.target);
    const type = this.getNodeTypeById(linkToDelete.source);
    const index = this.findIndex(this.template[type], linkToDelete.source);

    // immutable splice
    const updateNodes = [...this.template[type].slice(0, index),
      {...templateNode, [templateNode._type]: {...sourceNode, next: updatedRefs }},
      ...this.template[type].slice(index + 1)];

    this.updateTemplate({...this.template, [type]: updateNodes});
  }

  public deleteOrphanedLinks(deletedId: string) {
    ['inputs', 'processors', 'aggregators']
      .map(type => <TemplateNode[]>this.template[type])
      .forEach(nodesArr => {
        nodesArr.map(n => <Node>n[n._type])
          .forEach(node => {
            const index = this.findIndexOfLinkTo(node, deletedId);
            if (index < 0) {
              return;
            }
            node.next = [...node.next.slice(0, index), ...node.next.slice(index + 1)];
          });
      });
  }

  private findIndexOfLinkTo(node: Node, targetId: string): number {
    return node.next.map(nextWrapper => <NextRef>nextWrapper[nextWrapper._type])
      .findIndex((nextRef: NextRef) => nextRef.value.id === targetId);
  }

  private getNodeTypeById(id) {
    if (id.startsWith('in')) {
      return 'inputs';
    } else if (id.startsWith('pro')) {
      return 'processors';
    } else if (id.startsWith('agg')) {
      return 'aggregators';
    }

    return 'outputs';
  }

  private findIndex(nodes: TemplateNode[], idToFind: string) {
    return nodes.findIndex(n => (<Node>n[n._type]).id === idToFind);
  }

  private getNode(id: string): TemplateNode {
    let nodes: TemplateNode[] = [];

    if (id.startsWith('in')) {
      nodes = this.template.inputs.map(i => ({...i, nodeType: 'input'}));
    } else if (id.startsWith('pro')) {
      nodes = this.template.processors.map(p => ({...p, nodeType: 'processor'}));
    } else if (id.startsWith('agg')) {
      nodes = this.template.aggregators.map(a => ({...a, nodeType: 'aggregator'}));
    } else {
      nodes = this.template.outputs.map(o => ({...o, nodeType: 'output'}));
    }

    return nodes.find((n: TemplateNode) => (<Node>n[n._type]).id === id);
  }

  private updateSource(sourceId: string, newNext: NextWrapper): AgentTemplate {
    const sourceNodeWrapper = this.getNode(sourceId);
    const sourceNode = <Node>sourceNodeWrapper[sourceNodeWrapper._type];

    const targetId = (<NextRef>newNext[newNext._type]).value.id;
    const targetNode = this.getNode(targetId);

    const linkAlreadyExists = sourceNode.next.some(n => (<NextRef>n[n._type]).value.id === targetId);

    if (linkAlreadyExists) {
      throw new Error(`Link between '${sourceId}' and '${targetId}' already exist!`);
    }

    const introducesCycle = this.hasCycle(targetNode, sourceNode.id);

    if (introducesCycle) {
      throw new Error(`Adding link between '${sourceId}' and '${targetId}' would introduce a cycle!`);
    }

    sourceNode.next = [...sourceNode.next, newNext];
    sourceNodeWrapper[sourceNodeWrapper._type] = sourceNode;

    return {...this.template};
  }

  private hasCycle(startNode: TemplateNode, idToReach: string, remainingDepth = 10): boolean {
    if (remainingDepth < 0 || !startNode) {
      return false;
    }

    const n = <Node>startNode[startNode._type];
    if (n.id === idToReach) {
      return true;
    }
    if (!n.next || !n.next.length) {
      return false;
    }

    return n.next.some(next => {
      const id = (<NextRef>next[next._type]).value.id;
      const nextNode = this.getNode(id);
      return this.hasCycle(nextNode, idToReach, remainingDepth - 1);
    });
  }

  public uploadConfig() {
    const configStr = this.configProvider.generate(this.template);
    const configName = 'telegraf.conf';
    console.log(configStr);

    const fileUploads = [
      this.configFileHttpService.uploadConfig(configName, configStr),
      this.templateHttpService.uploadTemplate(this.savedTemplate)
    ];

    this.authHttpService.login(this.ccTokenProvider.ccToken)
      .pipe(
        tap((jwt: string) => this.authTokenProvider.token = jwt),
        switchMap(() => forkJoin(fileUploads)),
        tap(() => console.log(`Successfully uploaded template and config.`)),
        switchMap(() => this.authHttpService.logout()),
      ).subscribe(
      () => {},
      (err) => console.error(`Error uploading files: `, err)
      );
  }

  public goToNode(node: Node): Promise<boolean> {
    if (node && node.id) {
      return this.router.navigate(['/template'], { fragment: node.id });
    }
    return this.router.navigate(['/template']);
  }

}
