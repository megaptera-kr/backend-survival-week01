export async function parseResponse(response) {
  const { status: statusCode } = response;

  const result = await response.text();

  if (!result) {
    return {
      statusCode,
      data: {},
    };
  }

  return {
    statusCode,
    data: JSON.parse(result),
  };
}

// Todo: Delete this!
export function xxx() {
}
