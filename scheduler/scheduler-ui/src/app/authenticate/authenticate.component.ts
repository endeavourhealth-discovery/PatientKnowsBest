import {Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from "@angular/forms";
import {SchedulerService} from "../scheduler/scheduler.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-authenticate',
  templateUrl: './authenticate.component.html',
  styleUrls: ['./authenticate.component.css']
})
export class AuthenticateComponent implements OnInit {


  isSpin : boolean = false ;
 error : string = null ;

  constructor(private _service : SchedulerService,private router : Router) { }

  ngOnInit(): void {
  }

  onClick(form:NgForm){
    //when the user clicks login go and get the jwt token
    this.isSpin = true ;
    this._service.authenticate({username:form.value.username,password:form.value.password}).subscribe(value=>{
      console.log(value.jwtToken)
      this.error = null;
      this.isSpin = false ;
      this.router.navigate(["/schedule"])

    },error=>{
      this.isSpin = false;
      this.error = error;
    })
// console.log(this.form)
      // this.form.reset()

  }

}
