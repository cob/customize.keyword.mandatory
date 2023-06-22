# customize.keyword.mandatory

## Install

```bash
cob-cli customize mandatory

# restart recordm
```

## How to use:

```
Fields:
    field:
        name: field1
        description: $[value1,value2]
       
    field:
        name: other_field
        description: $mandatory(field1=value1) Mandatory if field 1 has value1 selected
        
    field:
        name: other_field
        description: $mandatory(field1!value1) Mandatory if field 1 has a value selected that is not value1
        
    field:
        name: other_field
        description: $mandatory(field1=) Mandatory if field 1 is empty
        
    field:
        name: other_field
        description: $mandatory(field1!) Mandatory if field 1 is not empty
    
```

## Build

```bash
cd others/recordm-validators
mvn clean package
cp target/cob-customize-mandatory-validators-*-SNAPSHOT.jar ../../recordm/bundles/
```