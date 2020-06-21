import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree,Router} from "@angular/router";
import {Injectable} from "@angular/core";
import {SchedulerService} from "../scheduler/scheduler.service";
import {map, take, tap} from "rxjs/operators";
import {Observable} from "rxjs";

@Injectable({providedIn:"root"})
export class LoginGuard implements  CanActivate {


  constructor(private _service:SchedulerService , private router : Router ) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean|UrlTree>| boolean | UrlTree {
  return this._service.user.pipe(take(1),map(user =>{
    if(user){
      return this.router.createUrlTree(['/schedule']);
    }else{
      return true;
    }
  }));


  }

}
