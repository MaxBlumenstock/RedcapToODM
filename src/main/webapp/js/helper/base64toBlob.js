/**
 * Converts byte characters to a byte array.
 * @param {type} byteCharacters
 * @return {Array} the byte array
 */
function getBlobDataAtOnce(byteCharacters) {
    byteNumbers = new Array(byteCharacters.length);
    for (var i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    byteArray = new Uint8Array(byteNumbers);
    return [byteArray];
}
/**
 * Converts a file that is encoded in base64 to a blob.
 * @param {type} base64Data The base64 String.
 * @param {type} contentType The type of the file eg. text or application/ocet-stream...
 * @return {Blob} The corresponding blob to the file.
 */
function base64toBlob(base64Data, contentType) {
    var byteCharacters,
            byteArray,
            byteNumbers,
            blobData,
            blob;
    contentType = contentType || '';
    byteCharacters = atob(base64Data);
    // Get blob data sliced or not
    blobData = getBlobDataAtOnce(byteCharacters);
    blob = new Blob(blobData, {type: contentType});
    return blob;
}