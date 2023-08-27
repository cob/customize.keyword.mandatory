# customize.keyword.mandatoryIf

## Install

```bash
cob-cli customize mandatoryIf

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
        description: $mandatoryIf(field1=value1) Mandatory if field 1 has value1 selected
        
    field:
        name: other_field
        description: $mandatoryIf(field1!=value1) Mandatory if field 1 has a value selected that is not value1
        
    field:
        name: other_field
        description: $mandatoryIf(field1=) Mandatory if field 1 is empty
        
    field:
        name: other_field
        description: $mandatoryIf(field1!) Mandatory if field 1 is not empty
    
```

For more information you can consult [this link](https://learning.cultofbits.com/docs/cob-platform/admins/managing-information/available-customizations/mandatory-fields/)

## Build

```bash
cd others/recordm-validators
mvn clean package
cp target/cob-customize-mandatoryif.jar ../../recordm/bundles/
```

## Release

1. Update `costumize.js` and increment version
2. Update `pom.xml` version
3. Build