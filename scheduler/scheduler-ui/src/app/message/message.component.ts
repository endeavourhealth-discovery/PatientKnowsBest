import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ConfiguredJob} from "../scheduler/respose.interfaces";

@Component({
  selector: 'app-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.css']
})
export class MessageComponent implements OnInit {

  message: String;

  constructor(@Inject(MAT_DIALOG_DATA) public data: String,
              public dialogRef: MatDialogRef<MessageComponent>,
  ) {
  }

  ngOnInit(): void {

    this.message = this.data;
  }

  onClick() {
    this.dialogRef.close();
  }

}
