# Transmitting large data between two spring boot app

Problem is describe on stackoverflow page [Question # 65262031](https://stackoverflow.com/questions/65262031/transmitting-large-data-between-two-spring-boot-app)

## Project Structure

- There is a `client` project which receives Multipart File
- The client project uses the `HttpClient` & `RequestEntity` to push data in chunk directly to `OutputStream`
- There is a `server` project which works with HttpServletRequest/Response (each having it own stream)
- `server` project has to kind of opertion `op1` and `op2`

For running any of the `op1` or `op2` operation use the postman configuration given in [postman.json](./add-ons/)

### op1

- Injects a 16bytes of random data into the response stream
- Reads data in chunk and count the size (_even though it is the same - in actual project it will change after some business operation is perform - it is skipped to avoid complexity_)
- Put the count of size in the response stream
- Put the chunk data in the response stream

On the `client` size the response received is then written into a `temp-folder`. 

### op2

- The file generated in the `temp-folder` is sent to the `client` which sends it over in chunk to `server`
- `server` is now reversing the process done by `op1` 
-- Stripes the initial 16bytes
-- Reads the next 6 bytes to find out the length of next bytes to be read
-- Reads the bytes as identified in the previous step.  

# Problem Facing Currently

1. Unable to understand if Spring WebFlux can solve this problem?
2. For smaller file `op1` works, but for large files it get stuck `op2` - somehow something is wrong with streams internally, but can't pinpoint it. See below images
3. `op2` doesn't work for any of the scenarios (smaller file or larger files)

## This one works

![WORKS](https://github.com/robin-carry/large-data-transfer/blob/main/add-ons/so-Upto1MB-transfer-works.png)

## This one doesn't (get stuck)

![WORKS](https://github.com/robin-carry/large-data-transfer/blob/main/add-ons/so-For6MB-transfer-gets-stuck.png)
