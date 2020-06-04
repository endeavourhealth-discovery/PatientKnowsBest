import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigureJobComponent } from './configure-job.component';

describe('ConfigureJobComponent', () => {
  let component: ConfigureJobComponent;
  let fixture: ComponentFixture<ConfigureJobComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigureJobComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigureJobComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
