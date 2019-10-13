val text = sc.textFile(raw"C:\\Users\george\Desktop\pelatologio.csv")
val dataset = importDatasetByNames(text, List("Name", "Age"), List(Datatype.STRING, Datatype.INTEGER), List("Id", "Name", "Age"))