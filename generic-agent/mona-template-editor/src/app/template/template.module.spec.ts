import { TemplateModule } from '@mona/template/template.module';

describe('TemplateModule', () => {
  let templateModule: TemplateModule;

  beforeEach(() => {
    templateModule = new TemplateModule();
  });

  it('should create an instance', () => {
    expect(templateModule).toBeTruthy();
  });
});
