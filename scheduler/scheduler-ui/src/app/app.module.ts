import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {HttpClientModule} from '@angular/common/http'
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SchedulerComponent } from './scheduler/scheduler.component';
import {SchedulerService}      from  './scheduler/scheduler.service';
import {ServerResponseCode}   from './scheduler/response.code.constants';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatRadioModule} from '@angular/material/radio';
import {MatTableModule} from "@angular/material/table";
import { NgxMatDatetimePickerModule, NgxMatTimepickerModule } from '@angular-material-components/datetime-picker';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import { LogtableComponent } from './logtable/logtable.component';
import {MatButtonModule} from "@angular/material/button";
import {MatDividerModule} from '@angular/material/divider';
import {MatDialogModule} from "@angular/material/dialog";
import { NewJobComponent } from './new-job/new-job.component';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {Routes,RouterModule} from "@angular/router";
import {MatTabsModule} from '@angular/material/tabs';
import {MatPaginatorModule} from "@angular/material/paginator";
import { ConfigureJobComponent } from './configure-job/configure-job.component';
import {MatIconModule} from '@angular/material/icon';


const routes : Routes = [{path:'' ,component : SchedulerComponent },

  {path:'history',component:LogtableComponent} ,

  {path:'configureJob',component:ConfigureJobComponent}]

@NgModule({
  declarations: [
    AppComponent,
    SchedulerComponent,
    LogtableComponent,
    NewJobComponent,
    ConfigureJobComponent,
    // SchedulerDropdownComponent

  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatRadioModule,
    NgxMatDatetimePickerModule,
    NgxMatTimepickerModule,
    MatDatepickerModule,
    MatSnackBarModule,
    MatTableModule,
    MatButtonModule,
    MatDividerModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    RouterModule.forRoot(routes),
    MatTabsModule,
    MatPaginatorModule,
    MatIconModule
  ],

  providers: [SchedulerService,ServerResponseCode,MatRadioModule],
  bootstrap: [AppComponent]
})
export class AppModule { }
