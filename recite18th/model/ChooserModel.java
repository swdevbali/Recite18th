/*
    Recite18th is a simple, easy to use and straightforward Java Database 
    Web Application Framework. See http://code.google.com/p/recite18th
    Copyright (C) 2011  Eko Suprapto Wibowo (swdev.bali@gmail.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/.

    History:
    - Apr 30, 2011 = 1st creation
      This class is use as the model of chooser view
      Each class must supply its pkfieldname, and label field.
      A PkFieldName is free, but by convention label field is nama_TABLE_NAME
      Not all table will be choose as foreign key, but recite18th by default will create
      the correct chooseView method. 
*/
package recite18th.model;
public class ChooserModel extends Model 
{
    public String id;
    public String label;

    public ChooserModel()
    {
        
    }
    public ChooserModel(String tableName, String pkFieldName)
    {
        this.tableName=tableName;
        this.pkFieldName=pkFieldName;
        fqn = ChooserModel.class.getName();
        plainClassName = "ChooserModel";
    }
    
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }
}