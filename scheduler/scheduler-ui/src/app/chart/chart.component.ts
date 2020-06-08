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

  @Input('dataset') data: { Success: number, Failed: number, Interrupted: number };

  @Input('tableData') tableData: LogDetails [];

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  dataSource = new MatTableDataSource<{ jobName: String, information: String, startTime: Date }>();



  public barChartLabels: Label[] = ['Success', 'Failed', 'Interrupted'];
  public barChartType: ChartType = 'bar';
  public barChartLegend = false;
  public barChartOptions: ChartOptions = {
    responsive: true,
    // We use these empty structures as placeholders for dynamic theming.
    scales: {xAxes: [{}], yAxes: [{ticks: {

          min : 0,
        }}]},
    plugins: {
      datalabels: {
        anchor: 'end',
        align: 'end',
      }
    }
  };

   _lineChartColors:Array<any> = [{
    backgroundColor: ['#28a745','#dc3545','#ffc107'],
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

    this.dataSource.paginator = this.paginator;
  }

  ngOnChanges(changes: SimpleChanges) {


    if (this.data) {
      console.log(this.data)
      this.status = true;
      this.barChartData = [{data: [this.data.Success, this.data.Failed, this.data.Interrupted]}
      ]
      this.dataSource.data = this.getData('Success');

    }


  }

  private getData(statusType: string) {
    let data: { jobName: String, information: String, startTime: Date } [] = [];

    this.tableData.forEach(i => {
      if (i.status == statusType) {
        data.push({jobName: i.job_name, information: i.information, startTime: i.job_start_time})
      }
    })
    return data;
  }


  onClick(event) {
    // console.log(event)
    if (event.active.length != 0) {
      if (event.active[0]._index == 0) {
        this.dataSource.data = this.getData("Success")
      } else if (event.active[0]._index == 1) {
        this.dataSource.data = this.getData("Failed")
      } else if (event.active[0]._index == 2) {
        this.dataSource.data = this.getData("Interrupted");
      }
    }


  }
}
