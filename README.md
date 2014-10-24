Preview Excel Files
=================

Requirements: 

The original system used PHPExcel to deal with the Excel files. But it cannot preview the data of large excel files like 500MB due to the huge memory consume. So I need to build a RESTful Service to deal with those uploaded excel files. 

=================

Implementation:

I used JAX-RS to create the RESTful service and it is based on MVC structure.

Based on the work of Nick Burch and Chris Lott, I successfully achieved preview function so that we can preview huge (500MB+) Xls, Xlsx Excel and CSV file's data during upload process and within limited memory. And then send those data back with JSON format.

I also achieved saving functions based on previous authors' work so that we can save the data of huge (500MB+) Xls, Xlsx excel and CSV files within limited memory according to user's requirements. For example, the user can specify which column or which row to start.

=================

Rianddy

rianddy@gmail.com
