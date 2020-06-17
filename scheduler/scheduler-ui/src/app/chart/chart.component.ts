import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {ChartOptions, ChartType, ChartDataSets} from 'chart.js';
import {Color, Label} from 'ng2-charts';
import {LogDetails} from "../scheduler/respose.interfaces";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";

@Component({
  selector: 'chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css']
})
export class ChartComponent implements OnInit, OnChanges {

  @Input('dataset') data: { Success: number, Failed: number, Interrupted: number,InterruptFailed:number };

   @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;


  public barChartLabels: Label[] = ['Success', 'Failed', 'Interrupted','Interrupt Failed'];
  public barChartType: ChartType = 'bar';
  public barChartLegend = false;
  public barChartOptions: ChartOptions = {
    responsive: true,
    // We use these empty structures as placeholders for dynamic theming.
    scales: {xAxes: [{}], yAxes: [{ticks: {

          min : 0
        }}]},
    plugins: {
      datalabels: {
        anchor: 'end',
        align: 'end',
      }
    }
  };

   _lineChartColors:Array<any> = [{
    backgroundColor: ['#28a745','#dc3545','#ffc107','#3ba8b9'],
    borderColor: 'blue',
    pointBackgroundColor: 'black',
    pointBorderColor: 'black',
    pointHoverBackgroundColor: 'red',
    pointHoverBorderColor: 'red'
  },
];


  public barChartData: ChartDataSets[];

  public status: boolean = false;

  constructor() {
  }

  ngOnInit(): void {


  }

  ngOnChanges(changes: SimpleChanges) {


    if (this.data) {
      console.log(this.data)
      this.status = true;

      this.barChartData = [{data: [this.data.Success, this.data.Failed, this.data.Interrupted,this.data.InterruptFailed]}
      ]


    }


  }





}
