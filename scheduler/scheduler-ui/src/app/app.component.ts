import {Component, OnDestroy, OnInit} from '@angular/core';
import {User} from "./authenticate/authenticate.model";
import {SchedulerService} from "./scheduler/scheduler.service";
import {Subscription} from "rxjs";
import {Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  user: User = null;
  userName: string = null;
  userSubscription: Subscription;
  isAuthenticated: boolean = false;
  expirationTimeOut: any;

  constructor(private _service: SchedulerService, private route: Router) {
  }

  ngOnInit() {

    this._service.autoLogin();

    this.userSubscription = this._service.user.subscribe(user => {
      this.isAuthenticated = user ? true : false;
      this.userName = user ? user.userName : "anonymous";
      // this.autoLogout(user)
      //    when the user logs in we need to start timer
      if (user) {
        this.autoLogout(user);
      }
    })


  }

  ngOnDestroy() {
    this.userSubscription.unsubscribe();
  }

  logout() {
    this._service.user.next(null);
    this.route.navigate(["/login"]);
    localStorage.removeItem('userData');
    if (this.expirationTimeOut) {
      clearTimeout(this.expirationTimeOut);
    }
  }


  private autoLogout(user: User) {
    this.expirationTimeOut = setTimeout(() => {
      this.logout();
    }, user.expiry);
  }
}
